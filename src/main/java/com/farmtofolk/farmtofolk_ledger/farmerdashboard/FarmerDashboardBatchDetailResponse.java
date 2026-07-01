package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import java.util.List;

public record FarmerDashboardBatchDetailResponse(
    BatchResponse batch,
    FarmResponse farm,
    List<TraceEventResponse> traceEvents,
    List<BatchUsageResponse> usage) {}
