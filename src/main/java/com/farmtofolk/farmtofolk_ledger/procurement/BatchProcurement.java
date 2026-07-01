package com.farmtofolk.farmtofolk_ledger.procurement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "batch_procurements")
@Deprecated
public class BatchProcurement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "batch_id", nullable = false, unique = true)
  private UUID batchId;

  @Column(name = "quantity_taken", nullable = false)
  private BigDecimal quantityTaken;

  @Column(name = "farmer_price_per_unit", nullable = false)
  private BigDecimal farmerPricePerUnit;

  @Column(name = "farmer_amount_payable", nullable = false)
  private BigDecimal farmerAmountPayable;

  @Column(name = "payment_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Column(nullable = false)
  private String currency = "INR";

  @Column(name = "procured_at", nullable = false)
  private LocalDateTime procuredAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    if (procuredAt == null) procuredAt = now;
    normalizeDefaultsAndCalculate();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
    normalizeDefaultsAndCalculate();
  }

  private void normalizeDefaultsAndCalculate() {
    currency =
        currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(Locale.ROOT);
    if (paymentStatus == null) {
      throw new IllegalStateException("paymentStatus must be UNPAID or PAID");
    }
    if (quantityTaken != null && farmerPricePerUnit != null) {
      farmerAmountPayable = quantityTaken.multiply(farmerPricePerUnit);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getBatchId() {
    return batchId;
  }

  public void setBatchId(UUID batchId) {
    this.batchId = batchId;
  }

  public BigDecimal getQuantityTaken() {
    return quantityTaken;
  }

  public void setQuantityTaken(BigDecimal quantityTaken) {
    this.quantityTaken = quantityTaken;
  }

  public BigDecimal getFarmerPricePerUnit() {
    return farmerPricePerUnit;
  }

  public void setFarmerPricePerUnit(BigDecimal farmerPricePerUnit) {
    this.farmerPricePerUnit = farmerPricePerUnit;
  }

  public BigDecimal getFarmerAmountPayable() {
    return farmerAmountPayable;
  }

  public void calculateFarmerAmountPayable() {
    this.farmerAmountPayable = quantityTaken.multiply(farmerPricePerUnit);
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency =
        currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(Locale.ROOT);
  }

  public LocalDateTime getProcuredAt() {
    return procuredAt;
  }

  public void setProcuredAt(LocalDateTime procuredAt) {
    this.procuredAt = procuredAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
