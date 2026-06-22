package com.farmtofolk.farmtofolk_ledger.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateFarmerRequest(
        @NotBlank
        String farmerCode,
        @NotBlank
        String name,
        @NotBlank
        String phone,
        @NotBlank
        String village,
        @NotBlank
        String district,
        @NotBlank
        String state,
        String bio,
        String profilePhotoUrl,
        String introVideoUrl,
        @NotNull
        LocalDate joinedDate
) {
}
