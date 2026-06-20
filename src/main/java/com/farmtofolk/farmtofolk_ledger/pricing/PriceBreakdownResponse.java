package com.farmtofolk.farmtofolk_ledger.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PriceBreakdownResponse(
        UUID id,
        UUID batchId,
        BigDecimal consumerPrice,
        BigDecimal farmerPrice,
        BigDecimal transportCost,
        BigDecimal packingCost,
        BigDecimal organizationCost,
        BigDecimal platformCost,
        String currency,
        String priceUnit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PriceBreakdownResponse from(PriceBreakdown priceBreakdown) {
        return new PriceBreakdownResponse(
                priceBreakdown.getId(),
                priceBreakdown.getBatchId(),
                priceBreakdown.getConsumerPrice(),
                priceBreakdown.getFarmerPrice(),
                priceBreakdown.getTransportCost(),
                priceBreakdown.getPackingCost(),
                priceBreakdown.getOrganizationCost(),
                priceBreakdown.getPlatformCost(),
                priceBreakdown.getCurrency(),
                priceBreakdown.getPriceUnit(),
                priceBreakdown.getCreatedAt(),
                priceBreakdown.getUpdatedAt()
        );
    }
}
