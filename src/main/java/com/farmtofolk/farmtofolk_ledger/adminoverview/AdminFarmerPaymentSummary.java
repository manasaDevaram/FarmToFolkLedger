package com.farmtofolk.farmtofolk_ledger.adminoverview;

import java.math.BigDecimal;

public record AdminFarmerPaymentSummary(
        BigDecimal totalPayable, BigDecimal totalPaid, BigDecimal totalPending) {
}
