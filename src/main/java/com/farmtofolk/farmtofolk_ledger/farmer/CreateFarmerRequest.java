package com.farmtofolk.farmtofolk_ledger.farmer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateFarmerRequest(
        String farmerCode,
        @NotBlank
        String name,
        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "must be 10 digits")
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
