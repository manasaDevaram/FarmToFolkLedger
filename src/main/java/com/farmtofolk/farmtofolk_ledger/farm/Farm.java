package com.farmtofolk.farmtofolk_ledger.farm;

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
@Table(name = "farms")
public class Farm {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "farmer_id", nullable = false)
  private UUID farmerId;

  @Column(name = "farm_name", nullable = false)
  private String farmName;

  private String village;

  private String district;

  private String state;

  private BigDecimal latitude;

  private BigDecimal longitude;

  @Column(name = "size_acres")
  private BigDecimal sizeAcres;

  @Column(name = "farming_type")
  private String farmingType;

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

  public UUID getFarmerId() {
    return farmerId;
  }

  public void setFarmerId(UUID farmerId) {
    this.farmerId = farmerId;
  }

  public String getFarmName() {
    return farmName;
  }

  public void setFarmName(String farmName) {
    this.farmName = farmName;
  }

  public String getVillage() {
    return village;
  }

  public void setVillage(String village) {
    this.village = village;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public BigDecimal getSizeAcres() {
    return sizeAcres;
  }

  public void setSizeAcres(BigDecimal sizeAcres) {
    this.sizeAcres = sizeAcres;
  }

  public String getFarmingType() {
    return farmingType;
  }

  public void setFarmingType(String farmingType) {
    this.farmingType = farmingType;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
