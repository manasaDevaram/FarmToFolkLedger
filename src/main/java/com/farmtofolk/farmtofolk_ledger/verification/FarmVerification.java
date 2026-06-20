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
}
