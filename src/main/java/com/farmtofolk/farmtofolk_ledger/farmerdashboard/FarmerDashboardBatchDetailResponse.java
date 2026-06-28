package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownResponse;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementResponse;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;

import java.math.BigDecimal;
import java.util.List;

public record FarmerDashboardBatchDetailResponse(
        BatchResponse batch,
        FarmResponse farm,
        List<TraceEventResponse> traceEvents,
        PriceBreakdownResponse priceBreakdown,
        BatchProcurementResponse procurement,
        List<BatchSaleTransactionResponse> saleTransactions,
        BigDecimal totalQuantitySold,
        BigDecimal quantityRemaining,
        BigDecimal totalSaleAmount
) {
}
