package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import java.time.LocalDate;

public record PublicTraceVerificationResponse(LocalDate verificationDate) {

  public static PublicTraceVerificationResponse from(FarmVerification verification) {
    return new PublicTraceVerificationResponse(verification.getVerificationDate());
  }
}
