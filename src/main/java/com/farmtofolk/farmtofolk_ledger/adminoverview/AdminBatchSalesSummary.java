package com.farmtofolk.farmtofolk_ledger.adminoverview;

import java.math.BigDecimal;

public record AdminBatchSalesSummary(
        BigDecimal totalSold, BigDecimal remaining, BigDecimal totalSaleAmount) {
}
