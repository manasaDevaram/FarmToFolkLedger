package com.farmtofolk.farmtofolk_ledger.farmer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "farmers")
public class Farmer {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "farmer_code", unique = true, nullable = false)
  private String farmerCode;

  @Column(nullable = false)
  private String name;

  @Column(unique = true)
  private String phone;

  private String village;

  private String district;

  private String state;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @Column(name = "profile_photo_url")
  private String profilePhotoUrl;

  @Column(name = "intro_video_url")
  private String introVideoUrl;

  @Column(name = "joined_date")
  private LocalDate joinedDate;

  private Boolean active = true;

  @Column(name = "user_id", unique = true)
  private UUID userId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;

    if (active == null) {
      active = true;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getFarmerCode() {
    return farmerCode;
  }

  public void setFarmerCode(String farmerCode) {
    this.farmerCode = farmerCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
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

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getProfilePhotoUrl() {
    return profilePhotoUrl;
  }

  public void setProfilePhotoUrl(String profilePhotoUrl) {
    this.profilePhotoUrl = profilePhotoUrl;
  }

  public String getIntroVideoUrl() {
    return introVideoUrl;
  }

  public void setIntroVideoUrl(String introVideoUrl) {
    this.introVideoUrl = introVideoUrl;
  }

  public LocalDate getJoinedDate() {
    return joinedDate;
  }

  public void setJoinedDate(LocalDate joinedDate) {
    this.joinedDate = joinedDate;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
