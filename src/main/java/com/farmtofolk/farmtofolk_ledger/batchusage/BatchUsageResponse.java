package com.farmtofolk.farmtofolk_ledger.batchusage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchUsageResponse(
    UUID id, UUID batchId, BatchUsageType usageType, BigDecimal quantity,
    BigDecimal pricePerUnit, String customerName, String customerType, String reason,
    String notes, LocalDateTime recordedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
  public static BatchUsageResponse from(BatchUsage usage) {
    return new BatchUsageResponse(
        usage.getId(), usage.getBatchId(), usage.getUsageType(), usage.getQuantity(),
        usage.getPricePerUnit(), usage.getCustomerName(), usage.getCustomerType(),
        usage.getReason(), usage.getNotes(), usage.getRecordedAt(), usage.getCreatedAt(),
        usage.getUpdatedAt());
  }
}
