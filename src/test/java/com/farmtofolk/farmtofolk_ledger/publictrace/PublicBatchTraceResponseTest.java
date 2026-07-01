package com.farmtofolk.farmtofolk_ledger.publictrace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class PublicBatchTraceResponseTest {
  @Test
  void publicBatchContractExposesCostsButNeverMargin() {
    var fields = Arrays.stream(PublicBatchTraceResponse.class.getRecordComponents())
        .map(component -> component.getName()).toList();

    assertTrue(fields.contains("farmerPricePerUnit"));
    assertTrue(fields.contains("consumerPricePerUnit"));
    assertTrue(fields.contains("farmToConsumerCostPerUnit"));
    assertFalse(fields.contains("margin"));
  }
}
