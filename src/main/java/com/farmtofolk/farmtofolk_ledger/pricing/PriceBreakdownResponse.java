package com.farmtofolk.farmtofolk_ledger.pricing;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PriceBreakdownResponse(
    UUID id,
    UUID batchId,
    BigDecimal consumerPrice,
    BigDecimal farmerPrice,
    BigDecimal wastageCost,
    BigDecimal packagingCost,
    BigDecimal operationalCost,
    BigDecimal margin,
    String currency,
    String priceUnit,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static PriceBreakdownResponse from(Batch batch) {
    return new PriceBreakdownResponse(
        batch.getId(),
        batch.getId(),
        batch.getConsumerPricePerUnit(),
        batch.getFarmerPricePerUnit(),
        batch.getWastageCost(),
        batch.getPackagingCost(),
        batch.getOperationalCostPerUnit(),
        batch.getMargin(),
        batch.getCurrency(),
        batch.getPriceUnit(),
        batch.getCreatedAt(),
        batch.getUpdatedAt());
  }
}
