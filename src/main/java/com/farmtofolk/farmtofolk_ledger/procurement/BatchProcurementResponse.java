package com.farmtofolk.farmtofolk_ledger.procurement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchProcurementResponse(
        UUID id,
        UUID batchId,
        BigDecimal quantityTaken,
        BigDecimal farmerPricePerUnit,
        BigDecimal farmerAmountPayable,
        String paymentStatus,
        String currency,
        LocalDateTime procuredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BatchProcurementResponse from(BatchProcurement procurement) {
        return new BatchProcurementResponse(
                procurement.getId(), procurement.getBatchId(), procurement.getQuantityTaken(),
                procurement.getFarmerPricePerUnit(), procurement.getFarmerAmountPayable(),
                procurement.getPaymentStatus(), procurement.getCurrency(), procurement.getProcuredAt(),
                procurement.getCreatedAt(), procurement.getUpdatedAt()
        );
    }
}
