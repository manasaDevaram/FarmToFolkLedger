package com.farmtofolk.farmtofolk_ledger.adminpayments;

import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementResponse;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminPaymentController {

  private final AdminPaymentService adminPaymentService;

  public AdminPaymentController(AdminPaymentService adminPaymentService) {
    this.adminPaymentService = adminPaymentService;
  }

  @GetMapping("/payments/summary")
  public List<FarmerPaymentSummaryResponse> getFarmerSummaries() {
    return adminPaymentService.getFarmerSummaries();
  }

  @GetMapping("/payments/batches")
  public List<AdminBatchPaymentResponse> getBatchPayments(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) UUID farmerId,
      @RequestParam(required = false) String cropName) {
    return adminPaymentService.getBatchPayments(status, farmerId, cropName);
  }

  @PatchMapping("/procurements/{procurementId}/payment-status")
  @Deprecated
  public BatchProcurementResponse updatePaymentStatus(
      @PathVariable UUID procurementId, @Valid @RequestBody UpdatePaymentStatusRequest request) {
    return adminPaymentService.updatePaymentStatus(procurementId, request);
  }

  @PatchMapping("/batches/{batchId}/payment-status")
  public BatchResponse updateBatchPaymentStatus(
      @PathVariable UUID batchId, @Valid @RequestBody UpdatePaymentStatusRequest request) {
    return adminPaymentService.updateBatchPaymentStatus(batchId, request);
  }
}
