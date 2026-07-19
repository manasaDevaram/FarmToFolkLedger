package com.farmtofolk.farmtofolk_ledger.pricing;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceBreakdownService {

  private final BatchRepository batchRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final AfterCommitExecutor afterCommitExecutor;

  public PriceBreakdownService(
      BatchRepository batchRepository,
      PublicTraceCacheService publicTraceCacheService,
      AfterCommitExecutor afterCommitExecutor) {
    this.batchRepository = batchRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.afterCommitExecutor = afterCommitExecutor;
  }

  public PriceBreakdownResponse createPriceBreakdown(
      UUID batchId, CreatePriceBreakdownRequest request) {
    Batch batch = findBatch(batchId);
    if (batch.hasDetailedPriceBreakdown()) {
      throw new ConflictException("Price breakdown already exists");
    }
    applyRequest(batch, request);
    Batch savedBatch = batchRepository.save(batch);
    evictPublicTraceAfterCommit(batchId);
    return PriceBreakdownResponse.from(savedBatch);
  }

  public PriceBreakdownResponse getPriceBreakdown(UUID batchId) {
    Batch batch = findBatch(batchId);
    if (!batch.hasDetailedPriceBreakdown()) {
      throw new ResourceNotFoundException("Price breakdown not found");
    }
    return PriceBreakdownResponse.from(batch);
  }

  public PriceBreakdownResponse updatePriceBreakdown(
      UUID batchId, CreatePriceBreakdownRequest request) {
    Batch batch = findBatch(batchId);
    applyRequest(batch, request);
    Batch savedBatch = batchRepository.save(batch);
    evictPublicTraceAfterCommit(batchId);
    return PriceBreakdownResponse.from(savedBatch);
  }

  private Batch findBatch(UUID batchId) {
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
  }

  private void applyRequest(Batch batch, CreatePriceBreakdownRequest request) {
    batch.setConsumerPricePerUnit(request.consumerPrice());
    batch.setFarmerPricePerUnit(request.farmerPrice());
    batch.setWastageCost(request.wastageCost());
    batch.setPackagingCost(request.packagingCost());
    batch.setOperationalCostPerUnit(request.operationalCost());
    batch.setCurrency(request.currency());
    batch.setPriceUnit(request.priceUnit());
    batch.calculateTotalFarmerAmount();
  }

  private void evictPublicTraceAfterCommit(UUID batchId) {
    afterCommitExecutor.run(() -> publicTraceCacheService.evictStableDataForBatch(batchId));
  }
}
