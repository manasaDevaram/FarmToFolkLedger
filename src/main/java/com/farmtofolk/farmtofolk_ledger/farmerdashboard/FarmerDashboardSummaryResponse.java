package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.auth.UserResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;

public record FarmerDashboardSummaryResponse(
        FarmerResponse farmer,
        UserResponse user,
        long farmsCount,
        long batchesCount
) {
}
