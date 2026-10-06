package com.pm.pharmacyservice.service;

import com.pm.pharmacyservice.dto.PrescriptionRequestDTO;
import com.pm.pharmacyservice.kafka.PharmacyKafkaProducer;
import com.pm.pharmacyservice.model.Medication;
import com.pm.pharmacyservice.model.Prescription;
import com.pm.pharmacyservice.repository.MedicationRepository;
import com.pm.pharmacyservice.repository.PrescriptionRepository;
import com.pm.pharmacyservice.dto.PharmacyOrderRequest;
import com.pm.pharmacyservice.dto.MedicationRequestDTO;
import com.pm.pharmacyservice.model.PharmacyOrder;
import com.pm.pharmacyservice.model.PharmacyOrderLine;
import com.pm.pharmacyservice.repository.PharmacyOrderRepository;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PharmacyService {

    private final MedicationRepository medicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyKafkaProducer kafkaProducer;
    private final PharmacyOrderRepository orderRepository;
    private final BillingClient billingClient;

    public PharmacyService(MedicationRepository medicationRepository,
                           PrescriptionRepository prescriptionRepository,
                           PharmacyKafkaProducer kafkaProducer,
                           PharmacyOrderRepository orderRepository,
                           BillingClient billingClient) {
        this.medicationRepository = medicationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.kafkaProducer = kafkaProducer;
        this.orderRepository = orderRepository;
        this.billingClient = billingClient;
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findApprovedForCatalogue();
    }

    public List<Medication> reviewQueue() { return medicationRepository.findByApprovalStatusOrderByNameAsc("PENDING_REVIEW"); }

    @Transactional
    public Medication reviewMedication(UUID id, String reviewer, String decision, String note) {
        Medication item = medicationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!"PENDING_REVIEW".equals(item.getApprovalStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This medicine is not awaiting review");
        if (reviewer.equalsIgnoreCase(item.getSubmittedBy())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A different staff member must review this submission");
        if (!List.of("APPROVED", "REJECTED").contains(decision)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose approve or reject");
        item.setApprovalStatus(decision); item.setReviewedBy(reviewer); item.setReviewNote(note);
        Medication saved = medicationRepository.save(item);
        if ("APPROVED".equals(decision)) kafkaProducer.sendInventoryEvent(saved, 0, "CREATED_APPROVED");
        return saved;
    }

    public List<Medication> searchMedications(String query, String category, boolean availableOnly) {
        String q = query == null ? "" : query.trim().toLowerCase();
        String c = category == null ? "" : category.trim().toLowerCase();
        return getAllMedications().stream()
                .filter(item -> q.isBlank() || item.getName().toLowerCase().contains(q) || item.getCode().toLowerCase().contains(q))
                .filter(item -> c.isBlank() || c.equals(item.getCategory() == null ? "" : item.getCategory().toLowerCase()))
                .filter(item -> !availableOnly || item.getStockQuantity() > 0).toList();
    }

    public Medication addMedication(Medication medication) {
        Medication saved = medicationRepository.save(medication);
        kafkaProducer.sendInventoryEvent(saved, 0, "CREATED");
        return saved;
    }

    public Medication addMedication(MedicationRequestDTO request, String submitter) {
        if (medicationRepository.existsByName(request.name()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A medication with this name already exists");
        if (medicationRepository.existsByCode(request.code()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A medication with this code already exists");
        Medication item = new Medication(null, request.name().trim(), request.code().trim(),
                request.stockQuantity(), request.reorderThreshold(), request.price());
        item.setCategory(request.category().trim()); item.setDescription(request.description());
        item.setRequiresPrescription(request.requiresPrescription());
        item.setApprovalStatus("PENDING_REVIEW"); item.setSubmittedBy(submitter);
        Medication saved = medicationRepository.save(item);
        return saved;
    }

    public Medication addMedication(MedicationRequestDTO request) { return addMedication(request, "system"); }

    @Transactional
    public Prescription createPrescription(PrescriptionRequestDTO request) {
        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with ID: " + request.getMedicationId()));

        Prescription prescription = new Prescription();
        prescription.setPatientId(request.getPatientId());
        prescription.setMedicationId(medication.getId());
        prescription.setMedicationName(medication.getName());
        prescription.setQuantity(request.getQuantity());
        prescription.setDosage(request.getDosage() != null ? request.getDosage() : "1 pill daily");
        prescription.setStatus("PENDING");

        Prescription saved = prescriptionRepository.save(prescription);
        kafkaProducer.sendPrescriptionEvent(saved);
        return saved;
    }

    @Transactional
    public Prescription dispensePrescription(UUID prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found with ID: " + prescriptionId));

        Medication medication = medicationRepository.findById(prescription.getMedicationId())
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));

        if (medication.getStockQuantity() < prescription.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for medication: " + medication.getName());
        }

        int previousStock = medication.getStockQuantity();
        medication.setStockQuantity(previousStock - prescription.getQuantity());
        medicationRepository.save(medication);
        kafkaProducer.sendInventoryEvent(medication, previousStock, "PRESCRIPTION_DISPENSED");

        prescription.setStatus("DISPENSED");
        Prescription updated = prescriptionRepository.save(prescription);

        kafkaProducer.sendPrescriptionEvent(updated);
        return updated;
    }

    public List<Prescription> getPrescriptionsByPatient(UUID patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Transactional
    public PharmacyOrder placeOrder(String email, UUID patientUserId, PharmacyOrderRequest request) {
        PharmacyOrder order = new PharmacyOrder();
        order.setPatientEmail(email);
        order.setDeliveryAddress(request.deliveryAddress().trim());
        BigDecimal total = BigDecimal.ZERO;
        for (var requested : request.items()) {
            Medication medication = medicationRepository.findForUpdate(requested.medicationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
            if (!"APPROVED".equals(medication.getApprovalStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This medicine is not available yet");
            if (medication.getStockQuantity() < requested.quantity())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for " + medication.getName());
            if (medication.isRequiresPrescription()) {
                if (requested.prescriptionId() == null)
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "A valid prescription is required for " + medication.getName());
                Prescription prescription = prescriptionRepository.findByIdAndPatientId(requested.prescriptionId(), patientUserId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Prescription does not belong to this account"));
                if (!prescription.getMedicationId().equals(medication.getId()) || prescription.getQuantity() < requested.quantity()
                        || !"PENDING".equals(prescription.getStatus()))
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Prescription is not valid for this order");
                prescription.setStatus("ORDERED");
                prescriptionRepository.save(prescription);
            } else if (requested.prescriptionId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prescription supplied for a medicine that does not require one");
            }
            int previousStock = medication.getStockQuantity();
            medication.setStockQuantity(previousStock - requested.quantity());
            medicationRepository.save(medication);
            kafkaProducer.sendInventoryEvent(medication, previousStock, "ORDER_PLACED");
            PharmacyOrderLine line = new PharmacyOrderLine();
            line.setMedicationId(medication.getId()); line.setMedicationName(medication.getName());
            line.setPrescriptionId(requested.prescriptionId());
            line.setQuantity(requested.quantity()); line.setUnitPrice(medication.getPrice());
            line.setLineTotal(medication.getPrice().multiply(BigDecimal.valueOf(requested.quantity())));
            order.addItem(line); total = total.add(line.getLineTotal());
        }
        order.setTotal(total);
        PharmacyOrder saved = orderRepository.save(order);
        billingClient.createInvoice(email, saved.getId().toString(), total);
        return saved;
    }

    public List<PharmacyOrder> getOrdersForPatient(String email) {
        return orderRepository.findByPatientEmailOrderByCreatedAtDesc(email);
    }

    public List<PharmacyOrder> getOrders() { return orderRepository.findAll(); }

    @Transactional
    public PharmacyOrder updateOrderStatus(UUID id, String status) {
        if (!List.of("PROCESSING", "DISPENSED", "CANCELLED").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported pharmacy order status");
        PharmacyOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if ("PROCESSING".equals(status) && !List.of("PAID", "PROCESSING").contains(order.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An order must be paid before processing");
        if ("DISPENSED".equals(status) && !"PROCESSING".equals(order.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An order must be processing before it can be dispensed");
        if ("CANCELLED".equals(order.getStatus()) || "DISPENSED".equals(order.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order can no longer be changed");
        if ("CANCELLED".equals(status)) {
            if ("DISPENSED".equals(order.getStatus()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A dispensed order cannot be cancelled");
            for (PharmacyOrderLine line : order.getItems()) {
                medicationRepository.findForUpdate(line.getMedicationId()).ifPresent(medication -> {
                    int previousStock = medication.getStockQuantity();
                    medication.setStockQuantity(medication.getStockQuantity() + line.getQuantity());
                    medicationRepository.save(medication);
                    kafkaProducer.sendInventoryEvent(medication, previousStock, "ORDER_CANCELLED");
                });
                if (line.getPrescriptionId() != null) prescriptionRepository.findById(line.getPrescriptionId())
                    .filter(prescription -> "ORDERED".equals(prescription.getStatus()))
                    .ifPresent(prescription -> { prescription.setStatus("PENDING"); prescriptionRepository.save(prescription); });
            }
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
