package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import java.util.List;

public record FarmerDashboardFarmResponse(
    FarmResponse farm, List<FarmerDashboardWorkBatchResponse> batches) {}
