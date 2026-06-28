package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import java.util.List;

public record AdminFarmerOverviewResponse(
        FarmerResponse farmer,
        List<FarmResponse> farms,
        List<BatchResponse> batches,
        AdminFarmerPaymentSummary paymentSummary) {
}
