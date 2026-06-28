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
public class PriceBreakdown {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "batch_id", nullable = false)
  private UUID batchId;

  @Column(name = "consumer_price")
  private BigDecimal consumerPrice;

  @Column(name = "farmer_price")
  private BigDecimal farmerPrice;

  @Column(name = "transport_cost")
  private BigDecimal transportCost;

  @Column(name = "packing_cost")
  private BigDecimal packingCost;

  @Column(name = "organization_cost")
  private BigDecimal organizationCost;

  @Column(name = "platform_cost")
  private BigDecimal platformCost;

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

  public BigDecimal getTransportCost() {
    return transportCost;
  }

  public void setTransportCost(BigDecimal transportCost) {
    this.transportCost = transportCost;
  }

  public BigDecimal getPackingCost() {
    return packingCost;
  }

  public void setPackingCost(BigDecimal packingCost) {
    this.packingCost = packingCost;
  }

  public BigDecimal getOrganizationCost() {
    return organizationCost;
  }

  public void setOrganizationCost(BigDecimal organizationCost) {
    this.organizationCost = organizationCost;
  }

  public BigDecimal getPlatformCost() {
    return platformCost;
  }

  public void setPlatformCost(BigDecimal platformCost) {
    this.platformCost = platformCost;
  }

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
