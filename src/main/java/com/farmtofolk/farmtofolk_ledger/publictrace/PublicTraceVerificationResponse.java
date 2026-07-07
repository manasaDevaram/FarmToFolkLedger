package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import java.time.LocalDate;

public record PublicTraceVerificationResponse(
    LocalDate verificationDate,
    String verificationType,
    Boolean chemicalFreeClaim,
    Boolean agroecologyVerified,
    String checklistJson,
    String observations,
    LocalDate nextVerificationDue) {

  public static PublicTraceVerificationResponse from(FarmVerification verification) {
    return new PublicTraceVerificationResponse(
        verification.getVerificationDate(),
        verification.getVerificationType(),
        verification.getChemicalFreeClaim(),
        verification.getAgroecologyVerified(),
        verification.getChecklistJson(),
        verification.getObservations(),
        verification.getNextVerificationDue());
  }
}
