package com.farmtofolk.farmtofolk_ledger.verification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmVerificationResponse(
    UUID id,
    UUID farmId,
    LocalDate verificationDate,
    UUID verifiedByUserId,
    String verificationType,
    String status,
    Boolean chemicalFreeClaim,
    Boolean agroecologyVerified,
    String checklistJson,
    String observations,
    LocalDate nextVerificationDue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static FarmVerificationResponse from(FarmVerification farmVerification) {
    return new FarmVerificationResponse(
        farmVerification.getId(),
        farmVerification.getFarmId(),
        farmVerification.getVerificationDate(),
        farmVerification.getVerifiedByUserId(),
        farmVerification.getVerificationType(),
        farmVerification.getStatus(),
        farmVerification.getChemicalFreeClaim(),
        farmVerification.getAgroecologyVerified(),
        farmVerification.getChecklistJson(),
        farmVerification.getObservations(),
        farmVerification.getNextVerificationDue(),
        farmVerification.getCreatedAt(),
        farmVerification.getUpdatedAt());
  }
}
