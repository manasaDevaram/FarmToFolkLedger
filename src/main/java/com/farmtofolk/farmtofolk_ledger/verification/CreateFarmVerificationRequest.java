package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateFarmVerificationRequest(
    @NotNull LocalDate verificationDate,
    UUID verifiedByUserId,
    @NotBlank String verificationType,
    @NotBlank String status,
    Boolean chemicalFreeClaim,
    Boolean agroecologyVerified,
    String checklistJson,
    String observations,
    LocalDate nextVerificationDue) {}
