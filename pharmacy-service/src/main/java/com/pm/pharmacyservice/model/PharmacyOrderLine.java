package com.pm.pharmacyservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "pharmacy_order_lines")
public class PharmacyOrderLine {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID medicationId;
  private UUID prescriptionId;
  @Column(nullable = false) private String medicationName;
  @Column(nullable = false) private Integer quantity;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal lineTotal;
  public UUID getId() { return id; }
  public UUID getMedicationId() { return medicationId; }
  public void setMedicationId(UUID v) { medicationId = v; }
  public UUID getPrescriptionId() { return prescriptionId; }
  public void setPrescriptionId(UUID v) { prescriptionId = v; }
  public String getMedicationName() { return medicationName; }
  public void setMedicationName(String v) { medicationName = v; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer v) { quantity = v; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public void setUnitPrice(BigDecimal v) { unitPrice = v; }
  public BigDecimal getLineTotal() { return lineTotal; }
  public void setLineTotal(BigDecimal v) { lineTotal = v; }
}
