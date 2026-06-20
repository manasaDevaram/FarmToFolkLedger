package com.farmtofolk.farmtofolk_ledger.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBatchRequest(
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
        String status
) {
}
