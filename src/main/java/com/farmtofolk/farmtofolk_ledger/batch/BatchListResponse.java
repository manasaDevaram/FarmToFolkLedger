package com.farmtofolk.farmtofolk_ledger.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchListResponse(
    UUID id,
    String batchCode,
    UUID farmerId,
    String farmerName,
    UUID farmId,
    String farmName,
    String cropName,
    String variety,
    BigDecimal quantity,
    String unit,
    LocalDate harvestDate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static BatchListResponse from(Batch batch, String farmerName, String farmName) {
    return new BatchListResponse(
        batch.getId(),
        batch.getBatchCode(),
        batch.getFarmerId(),
        farmerName,
        batch.getFarmId(),
        farmName,
        batch.getCropName(),
        batch.getVariety(),
        batch.getQuantity(),
        batch.getUnit(),
        batch.getHarvestDate(),
        batch.getStatus(),
        batch.getCreatedAt(),
        batch.getUpdatedAt());
  }
}
