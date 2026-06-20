package com.farmtofolk.farmtofolk_ledger.qr;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_codes")
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "public_token", unique = true, nullable = false)
    private String publicToken;

    @Column(name = "qr_image_url")
    private String qrImageUrl;

    @Column(name = "qr_type")
    private String qrType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
