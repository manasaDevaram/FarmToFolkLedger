package com.farmtofolk.farmtofolk_ledger.batchusage;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchUsageService {
  private final BatchUsageRepository usageRepository;
  private final BatchRepository batchRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final AfterCommitExecutor afterCommitExecutor;

  public BatchUsageService(
      BatchUsageRepository usageRepository,
      BatchRepository batchRepository,
      PublicTraceCacheService publicTraceCacheService,
      AfterCommitExecutor afterCommitExecutor) {
    this.usageRepository = usageRepository;
    this.batchRepository = batchRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.afterCommitExecutor = afterCommitExecutor;
  }

  @Transactional
  public BatchUsageResponse createUsage(UUID batchId, CreateBatchUsageRequest request) {
    Batch batch = batchRepository.findForUpdateById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    validate(request, batch);

    BatchUsage usage = new BatchUsage();
    usage.setBatchId(batchId);
    usage.setUsageType(request.usageType());
    usage.setQuantity(request.quantity());
    usage.setPricePerUnit(request.pricePerUnit());
    usage.setCustomerName(request.customerName());
    usage.setCustomerType(request.customerType());
    usage.setReason(request.reason());
    usage.setNotes(request.notes());
    usage.setRecordedAt(request.recordedAt());

    applyInventoryChange(batch, request.usageType(), request.quantity());
    batchRepository.save(batch);
    BatchUsage saved = usageRepository.save(usage);
    afterCommitExecutor.run(() -> publicTraceCacheService.evictStableDataForBatch(batchId));
    return BatchUsageResponse.from(saved);
  }

  @Transactional
  public BatchUsageResponse recordWaste(UUID batchId, CreateBatchWasteRequest request) {
    return createUsage(
        batchId,
        new CreateBatchUsageRequest(
            BatchUsageType.WASTED, request.quantity(), null, null, null,
            request.reason(), request.notes(), request.recordedAt()));
  }

  @Transactional(readOnly = true)
  public List<BatchUsageResponse> getUsage(UUID batchId) {
    if (!batchRepository.existsById(batchId)) {
      throw new ResourceNotFoundException("Batch not found");
    }
    return usageRepository.findByBatchIdOrderByRecordedAtAsc(batchId).stream()
        .map(BatchUsageResponse::from).toList();
  }

  private void validate(CreateBatchUsageRequest request, Batch batch) {
    if (request.usageType().requiresPrice() && request.pricePerUnit() == null) {
      throw new BadRequestException("pricePerUnit is required for sold usage");
    }
    BigDecimal available = batch.getQuantityAvailable() == null
        ? BigDecimal.ZERO : batch.getQuantityAvailable();
    if (request.quantity().compareTo(available) > 0) {
      throw new BadRequestException("Usage quantity exceeds available batch quantity");
    }
  }

  private void applyInventoryChange(Batch batch, BatchUsageType type, BigDecimal quantity) {
    if (type.isSale()) {
      batch.setQuantitySold(zero(batch.getQuantitySold()).add(quantity));
    } else if (type == BatchUsageType.USED_IN_PRODUCT) {
      batch.setQuantityUsedInProduct(zero(batch.getQuantityUsedInProduct()).add(quantity));
    } else if (type == BatchUsageType.WASTED) {
      batch.setQuantityWasted(zero(batch.getQuantityWasted()).add(quantity));
    }
    batch.setQuantityAvailable(batch.getQuantityAvailable().subtract(quantity));
  }

  private BigDecimal zero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
