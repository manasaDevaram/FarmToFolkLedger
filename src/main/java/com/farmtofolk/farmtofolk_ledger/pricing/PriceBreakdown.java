package com.farmtofolk.farmtofolk_ledger.pricing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_breakdowns")
@Deprecated
public class PriceBreakdown {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "batch_id", nullable = false, unique = true)
  private UUID batchId;

  @Column(name = "consumer_price")
  private BigDecimal consumerPrice;

  @Column(name = "farmer_price")
  private BigDecimal farmerPrice;

  @Column(name = "operational_cost", precision = 19, scale = 2)
  private BigDecimal operationalCost;

  @Column(name = "wastage_cost", precision = 19, scale = 2)
  private BigDecimal wastageCost = BigDecimal.ZERO;

  @Column(name = "packaging_cost", precision = 19, scale = 2)
  private BigDecimal packagingCost = BigDecimal.ZERO;

  private String currency = "INR";

  @Column(name = "price_unit")
  private String priceUnit;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
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

  public BigDecimal getConsumerPrice() {
    return consumerPrice;
  }

  public void setConsumerPrice(BigDecimal consumerPrice) {
    this.consumerPrice = consumerPrice;
  }

  public BigDecimal getFarmerPrice() {
    return farmerPrice;
  }

  public void setFarmerPrice(BigDecimal farmerPrice) {
    this.farmerPrice = farmerPrice;
  }

  public BigDecimal getOperationalCost() {
    return operationalCost;
  }

  public void setOperationalCost(BigDecimal operationalCost) {
    this.operationalCost = operationalCost;
  }

  public BigDecimal getWastageCost() { return wastageCost; }

  public void setWastageCost(BigDecimal wastageCost) { this.wastageCost = wastageCost; }

  public BigDecimal getPackagingCost() { return packagingCost; }

  public void setPackagingCost(BigDecimal packagingCost) { this.packagingCost = packagingCost; }

  public BigDecimal getMargin() {
    if (consumerPrice == null || farmerPrice == null) return null;
    return consumerPrice
        .subtract(farmerPrice)
        .subtract(zero(wastageCost))
        .subtract(zero(packagingCost))
        .subtract(zero(operationalCost));
  }

  private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getPriceUnit() {
    return priceUnit;
  }

  public void setPriceUnit(String priceUnit) {
    this.priceUnit = priceUnit;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
