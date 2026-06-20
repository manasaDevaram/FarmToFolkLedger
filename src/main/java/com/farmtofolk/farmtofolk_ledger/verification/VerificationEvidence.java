package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_evidence")
public class VerificationEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verification_id", nullable = false)
    private UUID verificationId;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
