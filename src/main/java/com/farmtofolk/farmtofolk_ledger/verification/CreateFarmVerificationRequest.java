package com.farmtofolk.farmtofolk_ledger.verification;

import java.time.LocalDate;
import java.util.UUID;

public record CreateFarmVerificationRequest(
        LocalDate verificationDate,
        UUID verifiedByUserId,
        String verificationType,
        String status,
        Boolean chemicalFreeClaim,
        Boolean agroecologyVerified,
        String checklistJson,
        String observations,
        LocalDate nextVerificationDue
) {
}
