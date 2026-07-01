package com.farmtofolk.farmtofolk_ledger.batchusage;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_usages")
public class BatchUsage {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "batch_id", nullable = false)
  private UUID batchId;
  @Enumerated(EnumType.STRING)
  @Column(name = "usage_type", nullable = false)
  private BatchUsageType usageType;
  @Column(nullable = false, precision = 19, scale = 3)
  private BigDecimal quantity;
  @Column(name = "price_per_unit", precision = 19, scale = 2)
  private BigDecimal pricePerUnit;
  @Column(name = "customer_name")
  private String customerName;
  @Column(name = "customer_type")
  private String customerType;
  private String reason;
  @Column(columnDefinition = "TEXT")
  private String notes;
  @Column(name = "recorded_at", nullable = false)
  private LocalDateTime recordedAt;
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (recordedAt == null) recordedAt = now;
    createdAt = now;
    updatedAt = now;
  }
  @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }

  public UUID getId() { return id; }
  public UUID getBatchId() { return batchId; }
  public void setBatchId(UUID batchId) { this.batchId = batchId; }
  public BatchUsageType getUsageType() { return usageType; }
  public void setUsageType(BatchUsageType usageType) { this.usageType = usageType; }
  public BigDecimal getQuantity() { return quantity; }
  public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
  public BigDecimal getPricePerUnit() { return pricePerUnit; }
  public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
  public String getCustomerName() { return customerName; }
  public void setCustomerName(String customerName) { this.customerName = customerName; }
  public String getCustomerType() { return customerType; }
  public void setCustomerType(String customerType) { this.customerType = customerType; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
  public LocalDateTime getRecordedAt() { return recordedAt; }
  public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
