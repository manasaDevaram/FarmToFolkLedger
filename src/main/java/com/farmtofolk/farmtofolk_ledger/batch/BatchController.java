package com.farmtofolk.farmtofolk_ledger.batch;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchController {

  private final BatchService batchService;

  public BatchController(BatchService batchService) {
    this.batchService = batchService;
  }

  @PostMapping("/api/batches")
  @ResponseStatus(HttpStatus.CREATED)
  public BatchResponse createBatch(@Valid @RequestBody CreateBatchRequest request) {
    return batchService.createBatch(request);
  }

  @GetMapping("/api/batches/{batchId}")
  public BatchResponse getBatch(@PathVariable UUID batchId) {
    return batchService.getBatch(batchId);
  }

  @GetMapping("/api/batches")
  public List<BatchListResponse> getAllBatches(
      @RequestParam(required = false) UUID farmerId,
      @RequestParam(required = false) UUID farmId,
      @RequestParam(required = false) String cropName,
      @RequestParam(required = false) String status) {
    return batchService.getAllBatches(farmerId, farmId, cropName, status);
  }

  @GetMapping("/api/farmers/{farmerId}/batches")
  public List<BatchResponse> getBatchesByFarmer(@PathVariable UUID farmerId) {
    return batchService.getBatchesByFarmer(farmerId);
  }

  @GetMapping("/api/farms/{farmId}/batches")
  public List<BatchResponse> getBatchesByFarm(@PathVariable UUID farmId) {
    return batchService.getBatchesByFarm(farmId);
  }

  @PutMapping("/api/batches/{batchId}")
  public BatchResponse updateBatch(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchRequest request) {
    return batchService.updateBatch(batchId, request);
  }
}
