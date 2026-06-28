package com.farmtofolk.farmtofolk_ledger.verification;

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
@Table(name = "farm_verifications")
public class FarmVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "farm_id", nullable = false)
  private UUID farmId;

  @Column(name = "verification_date")
  private LocalDate verificationDate;

  @Column(name = "verified_by_user_id")
  private UUID verifiedByUserId;

  @Column(name = "verification_type")
  private String verificationType;

  private String status;

  @Column(name = "chemical_free_claim")
  private Boolean chemicalFreeClaim;

  @Column(name = "agroecology_verified")
  private Boolean agroecologyVerified;

  @Column(name = "checklist_json", columnDefinition = "TEXT")
  private String checklistJson;

  @Column(columnDefinition = "TEXT")
  private String observations;

  @Column(name = "next_verification_due")
  private LocalDate nextVerificationDue;

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

  public UUID getFarmId() {
    return farmId;
  }

  public void setFarmId(UUID farmId) {
    this.farmId = farmId;
  }

  public LocalDate getVerificationDate() {
    return verificationDate;
  }

  public void setVerificationDate(LocalDate verificationDate) {
    this.verificationDate = verificationDate;
  }

  public UUID getVerifiedByUserId() {
    return verifiedByUserId;
  }

  public void setVerifiedByUserId(UUID verifiedByUserId) {
    this.verifiedByUserId = verifiedByUserId;
  }

  public String getVerificationType() {
    return verificationType;
  }

  public void setVerificationType(String verificationType) {
    this.verificationType = verificationType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getChemicalFreeClaim() {
    return chemicalFreeClaim;
  }

  public void setChemicalFreeClaim(Boolean chemicalFreeClaim) {
    this.chemicalFreeClaim = chemicalFreeClaim;
  }

  public Boolean getAgroecologyVerified() {
    return agroecologyVerified;
  }

  public void setAgroecologyVerified(Boolean agroecologyVerified) {
    this.agroecologyVerified = agroecologyVerified;
  }

  public String getChecklistJson() {
    return checklistJson;
  }

  public void setChecklistJson(String checklistJson) {
    this.checklistJson = checklistJson;
  }

  public String getObservations() {
    return observations;
  }

  public void setObservations(String observations) {
    this.observations = observations;
  }

  public LocalDate getNextVerificationDue() {
    return nextVerificationDue;
  }

  public void setNextVerificationDue(LocalDate nextVerificationDue) {
    this.nextVerificationDue = nextVerificationDue;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
