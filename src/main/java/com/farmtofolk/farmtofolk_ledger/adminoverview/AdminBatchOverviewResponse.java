package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownResponse;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementResponse;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import java.util.List;

public record AdminBatchOverviewResponse(
        BatchResponse batch,
        FarmerResponse farmer,
        FarmResponse farm,
        BatchProcurementResponse procurement,
        List<BatchSaleTransactionResponse> saleTransactions,
        AdminBatchSalesSummary salesSummary,
        PriceBreakdownResponse priceBreakdown,
        List<TraceEventResponse> traceEvents,
        QrCodeResponse qrCode) {
}
