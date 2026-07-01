package com.farmtofolk.farmtofolk_ledger.batchusage;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches/{batchId}")
public class BatchUsageController {
  private final BatchUsageService batchUsageService;

  public BatchUsageController(BatchUsageService batchUsageService) {
    this.batchUsageService = batchUsageService;
  }

  @PostMapping("/usage")
  @ResponseStatus(HttpStatus.CREATED)
  public BatchUsageResponse createUsage(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchUsageRequest request) {
    return batchUsageService.createUsage(batchId, request);
  }

  @GetMapping("/usage")
  public List<BatchUsageResponse> getUsage(@PathVariable UUID batchId) {
    return batchUsageService.getUsage(batchId);
  }

  @PostMapping("/waste")
  @ResponseStatus(HttpStatus.CREATED)
  public BatchUsageResponse recordWaste(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchWasteRequest request) {
    return batchUsageService.recordWaste(batchId, request);
  }
}
