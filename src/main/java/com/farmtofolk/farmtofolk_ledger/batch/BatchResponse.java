package com.farmtofolk.farmtofolk_ledger.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchResponse(
    UUID id,
    String batchCode,
    UUID farmId,
    UUID farmerId,
    String cropName,
    String variety,
    BigDecimal quantity,
    String unit,
    LocalDate harvestDate,
    LocalDate packedDate,
    LocalDate bestBeforeDate,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static BatchResponse from(Batch batch) {
    return new BatchResponse(
        batch.getId(),
        batch.getBatchCode(),
        batch.getFarmId(),
        batch.getFarmerId(),
        batch.getCropName(),
        batch.getVariety(),
        batch.getQuantity(),
        batch.getUnit(),
        batch.getHarvestDate(),
        batch.getPackedDate(),
        batch.getBestBeforeDate(),
        batch.getStatus(),
        batch.getCreatedAt(),
        batch.getUpdatedAt());
  }
}
