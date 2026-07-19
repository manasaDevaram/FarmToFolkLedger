package com.farmtofolk.farmtofolk_ledger.pricing;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceBreakdownController {

  private final PriceBreakdownService priceBreakdownService;

  public PriceBreakdownController(PriceBreakdownService priceBreakdownService) {
    this.priceBreakdownService = priceBreakdownService;
  }

  @PostMapping("/api/batches/{batchId}/price-breakdown")
  @ResponseStatus(HttpStatus.CREATED)
  public PriceBreakdownResponse createPriceBreakdown(
      @PathVariable UUID batchId, @Valid @RequestBody CreatePriceBreakdownRequest request) {
    return priceBreakdownService.createPriceBreakdown(batchId, request);
  }

  @GetMapping("/api/batches/{batchId}/price-breakdown")
  public PriceBreakdownResponse getPriceBreakdown(@PathVariable UUID batchId) {
    return priceBreakdownService.getPriceBreakdown(batchId);
  }

  @PutMapping("/api/batches/{batchId}/price-breakdown")
  public PriceBreakdownResponse updatePriceBreakdown(
      @PathVariable UUID batchId, @Valid @RequestBody CreatePriceBreakdownRequest request) {
    return priceBreakdownService.updatePriceBreakdown(batchId, request);
  }
}
