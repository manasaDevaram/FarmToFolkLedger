package com.farmtofolk.farmtofolk_ledger.sales;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batches/{batchId}/sale-transactions")
public class BatchSaleTransactionController {

  private final BatchSaleTransactionService saleTransactionService;

  public BatchSaleTransactionController(BatchSaleTransactionService saleTransactionService) {
    this.saleTransactionService = saleTransactionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BatchSaleTransactionResponse create(
      @PathVariable UUID batchId, @Valid @RequestBody CreateBatchSaleTransactionRequest request) {
    return saleTransactionService.create(batchId, request);
  }

  @GetMapping
  public List<BatchSaleTransactionResponse> getByBatch(@PathVariable UUID batchId) {
    return saleTransactionService.getByBatch(batchId);
  }
}
