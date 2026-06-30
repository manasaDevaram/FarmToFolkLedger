package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import java.util.List;

public record FarmerDashboardSummaryResponse(
    FarmerResponse farmer, List<FarmerDashboardFarmResponse> farms) {}
