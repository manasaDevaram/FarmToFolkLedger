package com.farmtofolk.farmtofolk_ledger.farm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFarmRequest(
        @NotNull
        UUID farmerId,
        @NotBlank
        String farmName,
        @NotBlank
        String village,
        @NotBlank
        String district,
        @NotBlank
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        @Positive
        BigDecimal sizeAcres,
        @NotBlank
        String farmingType
) {
}
