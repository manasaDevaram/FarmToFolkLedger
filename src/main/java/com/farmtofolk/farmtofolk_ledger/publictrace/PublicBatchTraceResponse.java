package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PublicBatchTraceResponse(
    UUID id,
    String batchCode,
    String cropName,
    String variety,
    String unit,
    LocalDate harvestDate,
    String status,
    BigDecimal farmerPricePerUnit,
    BigDecimal consumerPricePerUnit,
    BigDecimal farmToConsumerCostPerUnit) {
  public static PublicBatchTraceResponse from(BatchResponse batch) {
    return new PublicBatchTraceResponse(
        batch.id(), batch.batchCode(), batch.cropName(), batch.variety(), batch.unit(),
        batch.harvestDate(), batch.status(), batch.farmerPricePerUnit(),
        batch.consumerPricePerUnit(), batch.operationalCostPerUnit());
  }
}
