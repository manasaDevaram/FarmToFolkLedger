package com.farmtofolk.farmtofolk_ledger.adminoverview;

import java.util.UUID;
import java.util.List;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
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
    @Deprecated
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminOverviewService.getDashboardSummary();
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminOverviewService.getDashboard();
    }

    @GetMapping("/dashboard/pending-payments")
    public List<BatchResponse> getPendingPayments() {
        return adminOverviewService.getPendingPayments();
    }

    @GetMapping("/dashboard/pending-verifications")
    public List<FarmVerificationResponse> getPendingVerifications() {
        return adminOverviewService.getPendingVerifications();
    }

    @GetMapping("/dashboard/upcoming-verifications")
    public List<FarmVerificationResponse> getUpcomingVerifications() {
        return adminOverviewService.getUpcomingVerifications();
    }

    @GetMapping("/dashboard/batch-inventory")
    public List<BatchResponse> getBatchInventory() {
        return adminOverviewService.getBatchInventory();
    }

    @GetMapping("/dashboard/high-wastage-batches")
    public List<BatchResponse> getHighWastageBatches() {
        return adminOverviewService.getHighWastageBatches();
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
