package com.farmtofolk.farmtofolk_ledger.batch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "batches")
public class Batch {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "batch_code", unique = true, nullable = false)
  private String batchCode;

  @Column(name = "farm_id", nullable = false)
  private UUID farmId;

  @Column(name = "farmer_id", nullable = false)
  private UUID farmerId;

  @Column(name = "crop_name", nullable = false)
  private String cropName;

  private String variety;

  @Column(name = "quantity_received", nullable = false, precision = 19, scale = 3)
  private BigDecimal quantityReceived;

  @Column(name = "quantity_sold", nullable = false, precision = 19, scale = 3)
  private BigDecimal quantitySold = BigDecimal.ZERO;

  @Column(name = "quantity_wasted", nullable = false, precision = 19, scale = 3)
  private BigDecimal quantityWasted = BigDecimal.ZERO;

  @Column(name = "quantity_used_in_product", nullable = false, precision = 19, scale = 3)
  private BigDecimal quantityUsedInProduct = BigDecimal.ZERO;

  @Column(name = "quantity_available", nullable = false, precision = 19, scale = 3)
  private BigDecimal quantityAvailable;

  private String unit;

  @Column(name = "harvest_date")
  private LocalDate harvestDate;

  @Column(name = "received_date", nullable = false)
  private LocalDate receivedDate;

  @Column(name = "farmer_price_per_unit", nullable = false, precision = 19, scale = 2)
  private BigDecimal farmerPricePerUnit;

  @Column(name = "total_farmer_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalFarmerAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false)
  private PaymentStatus paymentStatus;

  @Column(name = "consumer_price_per_unit", nullable = false, precision = 19, scale = 2)
  private BigDecimal consumerPricePerUnit;

  @Column(name = "operational_cost_per_unit", nullable = false, precision = 19, scale = 2)
  private BigDecimal operationalCostPerUnit;

  private String status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    initializeAndCalculate();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
    calculateTotalFarmerAmount();
  }

  private void initializeAndCalculate() {
    quantitySold = quantitySold == null ? BigDecimal.ZERO : quantitySold;
    quantityWasted = quantityWasted == null ? BigDecimal.ZERO : quantityWasted;
    quantityUsedInProduct = quantityUsedInProduct == null ? BigDecimal.ZERO : quantityUsedInProduct;
    quantityAvailable = quantityAvailable == null ? quantityReceived : quantityAvailable;
    calculateTotalFarmerAmount();
  }

  public void calculateTotalFarmerAmount() {
    if (quantityReceived != null && farmerPricePerUnit != null) {
      totalFarmerAmount = quantityReceived.multiply(farmerPricePerUnit);
    }
  }

  public void initializeInventory() {
    quantitySold = BigDecimal.ZERO;
    quantityWasted = BigDecimal.ZERO;
    quantityUsedInProduct = BigDecimal.ZERO;
    quantityAvailable = quantityReceived;
    calculateTotalFarmerAmount();
  }

  public BigDecimal getMargin() {
    return consumerPricePerUnit.subtract(farmerPricePerUnit).subtract(operationalCostPerUnit);
  }

  public UUID getId() {
    return id;
  }

  public String getBatchCode() {
    return batchCode;
  }

  public void setBatchCode(String batchCode) {
    this.batchCode = batchCode;
  }

  public UUID getFarmId() {
    return farmId;
  }

  public void setFarmId(UUID farmId) {
    this.farmId = farmId;
  }

  public UUID getFarmerId() {
    return farmerId;
  }

  public void setFarmerId(UUID farmerId) {
    this.farmerId = farmerId;
  }

  public String getCropName() {
    return cropName;
  }

  public void setCropName(String cropName) {
    this.cropName = cropName;
  }

  public String getVariety() {
    return variety;
  }

  public void setVariety(String variety) {
    this.variety = variety;
  }

  public BigDecimal getQuantityReceived() {
    return quantityReceived;
  }

  public void setQuantityReceived(BigDecimal quantityReceived) {
    this.quantityReceived = quantityReceived;
  }

  /** @deprecated use quantityReceived. */
  @Deprecated
  public BigDecimal getQuantity() { return quantityReceived; }

  /** @deprecated use quantityReceived. */
  @Deprecated
  public void setQuantity(BigDecimal quantity) { this.quantityReceived = quantity; }

  public BigDecimal getQuantitySold() { return quantitySold; }
  public void setQuantitySold(BigDecimal quantitySold) { this.quantitySold = quantitySold; }
  public BigDecimal getQuantityWasted() { return quantityWasted; }
  public void setQuantityWasted(BigDecimal quantityWasted) { this.quantityWasted = quantityWasted; }
  public BigDecimal getQuantityUsedInProduct() { return quantityUsedInProduct; }
  public void setQuantityUsedInProduct(BigDecimal value) { this.quantityUsedInProduct = value; }
  public BigDecimal getQuantityAvailable() { return quantityAvailable; }
  public void setQuantityAvailable(BigDecimal quantityAvailable) { this.quantityAvailable = quantityAvailable; }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public LocalDate getHarvestDate() {
    return harvestDate;
  }

  public void setHarvestDate(LocalDate harvestDate) {
    this.harvestDate = harvestDate;
  }

  public LocalDate getReceivedDate() { return receivedDate; }
  public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
  public BigDecimal getFarmerPricePerUnit() { return farmerPricePerUnit; }
  public void setFarmerPricePerUnit(BigDecimal value) { this.farmerPricePerUnit = value; }
  public BigDecimal getTotalFarmerAmount() { return totalFarmerAmount; }
  public PaymentStatus getPaymentStatus() { return paymentStatus; }
  public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
  public BigDecimal getConsumerPricePerUnit() { return consumerPricePerUnit; }
  public void setConsumerPricePerUnit(BigDecimal value) { this.consumerPricePerUnit = value; }
  public BigDecimal getOperationalCostPerUnit() { return operationalCostPerUnit; }
  public void setOperationalCostPerUnit(BigDecimal value) { this.operationalCostPerUnit = value; }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
