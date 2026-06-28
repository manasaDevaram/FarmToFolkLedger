package com.farmtofolk.farmtofolk_ledger.procurement;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum PaymentStatus {
  UNPAID,
  PAID;

  @JsonCreator
  public static PaymentStatus from(String value) {
    if (value == null) {
      return null;
    }
    return PaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }
}
