package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import com.pm.patientservice.repository.MedicalRecordRepository;
import com.pm.patientservice.repository.PatientNotificationRepository;
import com.pm.patientservice.model.MedicalRecord;
import com.pm.patientservice.model.PatientNotification;
import com.pm.patientservice.dto.MedicalRecordRequest;
import com.pm.patientservice.dto.NotificationRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

  private final PatientRepository patientRepository;
  private final BillingServiceGrpcClient billingServiceGrpcClient;
  private final KafkaProducer kafkaProducer;
  private final MedicalRecordRepository medicalRecordRepository;
  private final PatientNotificationRepository notificationRepository;

  public PatientService(PatientRepository patientRepository,
      BillingServiceGrpcClient billingServiceGrpcClient,
      KafkaProducer kafkaProducer, MedicalRecordRepository medicalRecordRepository,
      PatientNotificationRepository notificationRepository) {
    this.patientRepository = patientRepository;
    this.billingServiceGrpcClient = billingServiceGrpcClient;
    this.kafkaProducer = kafkaProducer;
    this.medicalRecordRepository = medicalRecordRepository;
    this.notificationRepository = notificationRepository;
  }

  public List<PatientResponseDTO> getPatients() {
    List<Patient> patients = patientRepository.findAll();

    return patients.stream().map(PatientMapper::toDTO).toList();
  }

  public Optional<PatientResponseDTO> getPatientByEmail(String email) {
    return patientRepository.findByEmailIgnoreCase(email).map(PatientMapper::toDTO);
  }

  public boolean patientBelongsToEmail(UUID id, String email) {
    return patientRepository.findById(id).map(patient -> patient.getEmail().equalsIgnoreCase(email)).orElse(false);
  }

  public List<MedicalRecord> medicalHistory(String email) {
    return medicalRecordRepository.findByPatientEmailIgnoreCaseOrderByRecordedAtDesc(email);
  }

  public MedicalRecord addMedicalRecord(MedicalRecordRequest request) {
    MedicalRecord record = new MedicalRecord(); record.setPatientEmail(request.patientEmail().trim().toLowerCase());
    record.setRecordType(request.recordType().trim().toUpperCase()); record.setTitle(request.title().trim());
    record.setDetails(request.details().trim()); record.setProvider(request.provider());
    return medicalRecordRepository.save(record);
  }

  public List<PatientNotification> notifications(String email) {
    return notificationRepository.findByPatientEmailIgnoreCaseOrderByCreatedAtDesc(email);
  }

  public PatientNotification addNotification(NotificationRequest request) {
    PatientNotification notification = new PatientNotification();
    notification.setPatientEmail(request.patientEmail().trim().toLowerCase());
    notification.setType(request.type().trim().toUpperCase()); notification.setTitle(request.title().trim());
    notification.setMessage(request.message().trim());
    return notificationRepository.save(notification);
  }

  public PatientNotification markNotificationRead(String email, UUID id) {
    PatientNotification notification = notificationRepository.findByIdAndPatientEmailIgnoreCase(id, email)
        .orElseThrow(() -> new PatientNotFoundException("Notification not found"));
    notification.setRead(true); return notificationRepository.save(notification);
  }

  public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
    if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
      throw new EmailAlreadyExistsException(
          "A patient with this email " + "already exists"
              + patientRequestDTO.getEmail());
    }

    Patient newPatient = patientRepository.save(
        PatientMapper.toModel(patientRequestDTO));

    billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
        newPatient.getName(), newPatient.getEmail());

    kafkaProducer.sendEvent(newPatient);

    return PatientMapper.toDTO(newPatient);
  }

  public PatientResponseDTO updatePatient(UUID id,
      PatientRequestDTO patientRequestDTO) {

    Patient patient = patientRepository.findById(id).orElseThrow(
        () -> new PatientNotFoundException("Patient not found with ID: " + id));

    if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),
        id)) {
      throw new EmailAlreadyExistsException(
          "A patient with this email " + "already exists"
              + patientRequestDTO.getEmail());
    }

    patient.setName(patientRequestDTO.getName());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
    patient.setMobile(patientRequestDTO.getMobile());
    patient.setGender(patientRequestDTO.getGender());
    patient.setEmergencyContact(patientRequestDTO.getEmergencyContact());

    Patient updatedPatient = patientRepository.save(patient);
    return PatientMapper.toDTO(updatedPatient);
  }

  public void deletePatient(UUID id) {
    patientRepository.deleteById(id);
  }
}
