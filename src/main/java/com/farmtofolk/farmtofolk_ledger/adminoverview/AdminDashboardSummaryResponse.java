package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardSummaryResponse(
        long totalFarmers,
        long activeFarmers,
        long totalFarms,
        long totalBatches,
        BigDecimal pendingPaymentsAmount,
        long pendingPaymentBatchCount,
        List<FarmVerificationResponse> recentVerifications,
        long totalQrCodes) {
}
