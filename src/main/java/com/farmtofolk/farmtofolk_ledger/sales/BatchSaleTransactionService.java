package com.farmtofolk.farmtofolk_ledger.sales;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.auth.UserRole;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Deprecated
public class BatchSaleTransactionService {

  private final BatchSaleTransactionRepository saleTransactionRepository;
  private final BatchProcurementRepository procurementRepository;
  private final BatchRepository batchRepository;
  private final FarmerRepository farmerRepository;
  private final CurrentUserService currentUserService;

  public BatchSaleTransactionService(
      BatchSaleTransactionRepository saleTransactionRepository,
      BatchProcurementRepository procurementRepository,
      BatchRepository batchRepository,
      FarmerRepository farmerRepository,
      CurrentUserService currentUserService) {
    this.saleTransactionRepository = saleTransactionRepository;
    this.procurementRepository = procurementRepository;
    this.batchRepository = batchRepository;
    this.farmerRepository = farmerRepository;
    this.currentUserService = currentUserService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  public BatchSaleTransactionResponse create(
      UUID batchId, CreateBatchSaleTransactionRequest request) {
    findBatch(batchId);
    BatchProcurement procurement =
        procurementRepository
            .findByBatchIdForUpdate(batchId)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        "Procurement must be recorded before sale transactions"));

    BigDecimal soldAlready =
        saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId).stream()
            .map(BatchSaleTransaction::getQuantitySold)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (soldAlready.add(request.quantitySold()).compareTo(procurement.getQuantityTaken()) > 0) {
      throw new BadRequestException("Total sold quantity cannot exceed quantity taken");
    }

    BatchSaleTransaction transaction = new BatchSaleTransaction();
    transaction.setBatchId(batchId);
    transaction.setQuantitySold(request.quantitySold());
    transaction.setSalePricePerUnit(request.salePricePerUnit());
    transaction.calculateSaleAmount();
    transaction.setCurrency(request.currency());
    transaction.setSoldAt(request.soldAt());
    return BatchSaleTransactionResponse.from(saleTransactionRepository.save(transaction));
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','FARMER')")
  public List<BatchSaleTransactionResponse> getByBatch(UUID batchId) {
    Batch batch = findBatch(batchId);
    enforceFarmerOwnership(batch);
    return saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId).stream()
        .map(BatchSaleTransactionResponse::from)
        .toList();
  }

  private Batch findBatch(UUID batchId) {
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
  }

  private void enforceFarmerOwnership(Batch batch) {
    if (!UserRole.FARMER.equals(currentUserService.getCurrentUser().getRole())) return;
    boolean ownsBatch =
        farmerRepository
            .findByUserId(currentUserService.getCurrentUserId())
            .map(farmer -> farmer.getId().equals(batch.getFarmerId()))
            .orElse(false);
    if (!ownsBatch) throw new AccessDeniedException("You cannot access another farmer's batch");
  }
}
