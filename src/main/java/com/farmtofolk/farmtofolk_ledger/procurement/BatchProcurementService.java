package com.farmtofolk.farmtofolk_ledger.procurement;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.auth.UserRole;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransaction;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BatchProcurementService {

  private final BatchProcurementRepository procurementRepository;
  private final BatchRepository batchRepository;
  private final FarmerRepository farmerRepository;
  private final CurrentUserService currentUserService;
  private final BatchSaleTransactionRepository saleTransactionRepository;

  public BatchProcurementService(
      BatchProcurementRepository procurementRepository,
      BatchRepository batchRepository,
      FarmerRepository farmerRepository,
      CurrentUserService currentUserService,
      BatchSaleTransactionRepository saleTransactionRepository) {
    this.procurementRepository = procurementRepository;
    this.batchRepository = batchRepository;
    this.farmerRepository = farmerRepository;
    this.currentUserService = currentUserService;
    this.saleTransactionRepository = saleTransactionRepository;
  }

  @PreAuthorize("hasRole('ADMIN')")
  public BatchProcurementResponse create(UUID batchId, CreateBatchProcurementRequest request) {
    findBatch(batchId);
    if (procurementRepository.existsByBatchId(batchId)) {
      throw new ConflictException("Procurement already exists for this batch");
    }
    BatchProcurement procurement = new BatchProcurement();
    procurement.setBatchId(batchId);
    applyRequest(procurement, request, true);
    return BatchProcurementResponse.from(procurementRepository.save(procurement));
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','FARMER')")
  public BatchProcurementResponse get(UUID batchId) {
    Batch batch = findBatch(batchId);
    enforceFarmerOwnership(batch);
    return procurementRepository
        .findByBatchId(batchId)
        .map(BatchProcurementResponse::from)
        .orElseThrow(() -> new ResourceNotFoundException("Procurement not found"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  public BatchProcurementResponse update(UUID batchId, CreateBatchProcurementRequest request) {
    findBatch(batchId);
    BatchProcurement procurement =
        procurementRepository
            .findByBatchIdForUpdate(batchId)
            .orElseThrow(() -> new ResourceNotFoundException("Procurement not found"));
    BigDecimal totalSold =
        saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId).stream()
            .map(BatchSaleTransaction::getQuantitySold)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (request.quantityTaken().compareTo(totalSold) < 0) {
      throw new BadRequestException("Quantity taken cannot be less than total sold quantity");
    }
    applyRequest(procurement, request, false);
    return BatchProcurementResponse.from(procurementRepository.save(procurement));
  }

  private void applyRequest(
      BatchProcurement procurement,
      CreateBatchProcurementRequest request,
      boolean includeProcuredAt) {
    procurement.setQuantityTaken(request.quantityTaken());
    procurement.setFarmerPricePerUnit(request.farmerPricePerUnit());
    procurement.calculateFarmerAmountPayable();
    procurement.setPaymentStatus(request.paymentStatus());
    procurement.setCurrency(request.currency());
    if (includeProcuredAt) procurement.setProcuredAt(request.procuredAt());
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
