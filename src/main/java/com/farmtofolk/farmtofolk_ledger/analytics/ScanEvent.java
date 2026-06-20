package com.farmtofolk.farmtofolk_ledger.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scan_events")
public class ScanEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "qr_code_id", nullable = false)
    private UUID qrCodeId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    private String city;

    private String state;

    private String country;
}
