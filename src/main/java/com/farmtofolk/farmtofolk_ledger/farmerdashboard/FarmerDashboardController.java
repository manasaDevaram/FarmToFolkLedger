package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/farmer-dashboard/me")
public class FarmerDashboardController {

    private final FarmerDashboardService farmerDashboardService;

    public FarmerDashboardController(FarmerDashboardService farmerDashboardService) {
        this.farmerDashboardService = farmerDashboardService;
    }

    @GetMapping
    public FarmerDashboardSummaryResponse getSummary(@RequestParam(required = false) UUID farmerId) {
        return farmerDashboardService.getSummary(farmerId);
    }

    @GetMapping("/farms")
    public List<FarmResponse> getFarms(@RequestParam(required = false) UUID farmerId) {
        return farmerDashboardService.getFarms(farmerId);
    }

    @GetMapping("/batches")
    public List<FarmerDashboardBatchResponse> getBatches(@RequestParam(required = false) UUID farmerId) {
        return farmerDashboardService.getBatches(farmerId);
    }

    @GetMapping("/batches/{batchId}")
    public FarmerDashboardBatchDetailResponse getBatchDetail(@PathVariable UUID batchId) {
        return farmerDashboardService.getBatchDetail(batchId);
    }
}
