package com.pm.pharmacyservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "pharmacy_orders")
public class PharmacyOrder {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false, length = 500) private String deliveryAddress;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
  @Column(nullable = false) private String currency = "INR";
  @Column(nullable = false) private String status = "AWAITING_PAYMENT";
  @Column(nullable = false) private Instant createdAt;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PharmacyOrderLine> items = new ArrayList<>();
  @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getDeliveryAddress() { return deliveryAddress; }
  public void setDeliveryAddress(String v) { deliveryAddress = v; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal v) { total = v; }
  public String getCurrency() { return currency; }
  public String getStatus() { return status; }
  public void setStatus(String v) { status = v; }
  public Instant getCreatedAt() { return createdAt; }
  public List<PharmacyOrderLine> getItems() { return items; }
  public void addItem(PharmacyOrderLine item) { items.add(item); }
}
