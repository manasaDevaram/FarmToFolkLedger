package com.farmtofolk.farmtofolk_ledger.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreatePriceBreakdownRequest(
        @NotNull
        @PositiveOrZero
        BigDecimal consumerPrice,
        @NotNull
        @PositiveOrZero
        BigDecimal farmerPrice,
        @PositiveOrZero
        BigDecimal transportCost,
        @PositiveOrZero
        BigDecimal packingCost,
        @PositiveOrZero
        BigDecimal organizationCost,
        @PositiveOrZero
        BigDecimal platformCost,
        @NotBlank
        String currency,
        @NotBlank
        String priceUnit
) {
}
