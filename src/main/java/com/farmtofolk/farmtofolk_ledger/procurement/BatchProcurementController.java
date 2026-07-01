package com.farmtofolk.farmtofolk_ledger.procurement;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batches/{batchId}/procurement")
@Deprecated
public class BatchProcurementController {

  private final BatchProcurementService procurementService;

  public BatchProcurementController(BatchProcurementService procurementService) {
    this.procurementService = procurementService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BatchProcurementResponse create(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchProcurementRequest request) {
    return procurementService.create(batchId, request);
  }

  @GetMapping
  public BatchProcurementResponse get(@PathVariable UUID batchId) {
    return procurementService.get(batchId);
  }

  @PutMapping
  public BatchProcurementResponse update(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchProcurementRequest request) {
    return procurementService.update(batchId, request);
  }
}
