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

    private BigDecimal quantity;

    private String unit;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "packed_date")
    private LocalDate packedDate;

    @Column(name = "best_before_date")
    private LocalDate bestBeforeDate;

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
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
