package com.farmtofolk.farmtofolk_ledger.pricing;

import java.math.BigDecimal;

public record CreatePriceBreakdownRequest(
        BigDecimal consumerPrice,
        BigDecimal farmerPrice,
        BigDecimal transportCost,
        BigDecimal packingCost,
        BigDecimal organizationCost,
        BigDecimal platformCost,
        String currency,
        String priceUnit
) {
}
