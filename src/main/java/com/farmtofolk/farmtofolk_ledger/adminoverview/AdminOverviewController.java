package com.farmtofolk.farmtofolk_ledger.adminoverview;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminOverviewController {

    private final AdminOverviewService adminOverviewService;

    public AdminOverviewController(AdminOverviewService adminOverviewService) {
        this.adminOverviewService = adminOverviewService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminOverviewService.getDashboardSummary();
    }

    @GetMapping("/farmers/{farmerId}/overview")
    public AdminFarmerOverviewResponse getFarmerOverview(@PathVariable UUID farmerId) {
        return adminOverviewService.getFarmerOverview(farmerId);
    }

    @GetMapping("/farms/{farmId}/overview")
    public AdminFarmOverviewResponse getFarmOverview(@PathVariable UUID farmId) {
        return adminOverviewService.getFarmOverview(farmId);
    }

    @GetMapping("/batches/{batchId}/overview")
    public AdminBatchOverviewResponse getBatchOverview(@PathVariable UUID batchId) {
        return adminOverviewService.getBatchOverview(batchId);
    }
}
