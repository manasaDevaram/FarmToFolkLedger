package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageResponse;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import java.math.BigDecimal;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import java.util.List;

public record AdminBatchOverviewResponse(
        BatchResponse batch,
        FarmerResponse farmer,
        FarmResponse farm,
        List<BatchUsageResponse> usage,
        AdminBatchSalesSummary salesSummary,
        BigDecimal margin,
        List<TraceEventResponse> traceEvents,
        QrCodeResponse qrCode) {
}
