package com.farmtofolk.farmtofolk_ledger.pricing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreatePriceBreakdownRequest(
    @NotNull @PositiveOrZero BigDecimal consumerPrice,
    @NotNull @PositiveOrZero BigDecimal farmerPrice,
    @NotNull @PositiveOrZero BigDecimal operationalCost,
    @NotBlank String currency,
    @NotBlank String priceUnit) {}
