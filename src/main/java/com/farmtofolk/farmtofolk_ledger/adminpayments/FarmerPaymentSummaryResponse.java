package com.farmtofolk.farmtofolk_ledger.adminpayments;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FarmerPaymentSummaryResponse(
        UUID farmerId,
        String farmerName,
        String farmerPhone,
        BigDecimal totalPayable,
        BigDecimal totalPaid,
        BigDecimal totalPending,
        long pendingBatchCount,
        List<FarmerPaymentBatchResponse> batches
) {
}
