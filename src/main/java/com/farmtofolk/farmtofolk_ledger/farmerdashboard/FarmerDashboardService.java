package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.auth.*;
import com.farmtofolk.farmtofolk_ledger.batch.*;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farm.*;
import com.farmtofolk.farmtofolk_ledger.farmer.*;
import com.farmtofolk.farmtofolk_ledger.pricing.*;
import com.farmtofolk.farmtofolk_ledger.procurement.*;
import com.farmtofolk.farmtofolk_ledger.sales.*;
import com.farmtofolk.farmtofolk_ledger.traceability.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN','FARMER')")
public class FarmerDashboardService {

  private final CurrentUserService currentUserService;
  private final UserRepository userRepository;
  private final FarmerRepository farmerRepository;
  private final FarmRepository farmRepository;
  private final BatchRepository batchRepository;
  private final TraceEventRepository traceEventRepository;
  private final PriceBreakdownRepository priceBreakdownRepository;
  private final BatchProcurementRepository procurementRepository;
  private final BatchSaleTransactionRepository saleTransactionRepository;

  public FarmerDashboardService(
      CurrentUserService currentUserService,
      UserRepository userRepository,
      FarmerRepository farmerRepository,
      FarmRepository farmRepository,
      BatchRepository batchRepository,
      TraceEventRepository traceEventRepository,
      PriceBreakdownRepository priceBreakdownRepository,
      BatchProcurementRepository procurementRepository,
      BatchSaleTransactionRepository saleTransactionRepository) {
    this.currentUserService = currentUserService;
    this.userRepository = userRepository;
    this.farmerRepository = farmerRepository;
    this.farmRepository = farmRepository;
    this.batchRepository = batchRepository;
    this.traceEventRepository = traceEventRepository;
    this.priceBreakdownRepository = priceBreakdownRepository;
    this.procurementRepository = procurementRepository;
    this.saleTransactionRepository = saleTransactionRepository;
  }

  public FarmerDashboardSummaryResponse getSummary(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    UserResponse linkedUser =
        farmer.getUserId() == null
            ? null
            : userRepository.findById(farmer.getUserId()).map(UserResponse::from).orElse(null);
    return new FarmerDashboardSummaryResponse(
        FarmerResponse.from(farmer),
        linkedUser,
        farmRepository.countByFarmerId(farmer.getId()),
        batchRepository.countByFarmerId(farmer.getId()));
  }

  public List<FarmResponse> getFarms(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    return farmRepository.findByFarmerId(farmer.getId()).stream().map(FarmResponse::from).toList();
  }

  public List<FarmerDashboardBatchResponse> getBatches(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    List<Batch> batches = batchRepository.findByFarmerId(farmer.getId());
    Map<UUID, Farm> farms =
        farmRepository
            .findAllById(batches.stream().map(Batch::getFarmId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farm::getId, Function.identity()));

    return batches.stream()
        .map(
            batch -> {
              List<TraceEventResponse> events =
                  traceEventRepository.findByBatchIdOrderByEventTimeAsc(batch.getId()).stream()
                      .map(TraceEventResponse::from)
                      .toList();
              String latestTraceStatus = events.isEmpty() ? null : events.getLast().eventType();
              BatchProcurement procurement =
                  procurementRepository.findByBatchId(batch.getId()).orElse(null);
              List<BatchSaleTransaction> sales =
                  saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batch.getId());
              BigDecimal totalQuantitySold = sumQuantitySold(sales);
              BigDecimal totalSaleAmount = sumSaleAmount(sales);
              BigDecimal quantityTaken =
                  procurement == null ? BigDecimal.ZERO : procurement.getQuantityTaken();
              Farm farm = farms.get(batch.getFarmId());
              return new FarmerDashboardBatchResponse(
                  batch.getId(),
                  batch.getBatchCode(),
                  batch.getCropName(),
                  batch.getVariety(),
                  farm == null ? null : farm.getFarmName(),
                  quantityTaken,
                  batch.getUnit(),
                  batch.getStatus(),
                  batch.getHarvestDate(),
                  batch.getPackedDate(),
                  batch.getBestBeforeDate(),
                  latestTraceStatus,
                  procurement == null ? null : procurement.getFarmerPricePerUnit(),
                  procurement == null ? null : procurement.getFarmerAmountPayable(),
                  procurement == null ? null : procurement.getPaymentStatus(),
                  totalQuantitySold,
                  quantityTaken.subtract(totalQuantitySold),
                  totalSaleAmount,
                  procurement == null ? null : procurement.getCurrency());
            })
        .toList();
  }

  public FarmerDashboardBatchDetailResponse getBatchDetail(UUID batchId) {
    Batch batch =
        batchRepository
            .findById(batchId)
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    enforceOwnership(batch);
    Farm farm =
        farmRepository
            .findById(batch.getFarmId())
            .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

    BatchProcurement procurement = procurementRepository.findByBatchId(batchId).orElse(null);
    List<BatchSaleTransaction> sales =
        saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId);
    BigDecimal totalQuantitySold = sumQuantitySold(sales);
    BigDecimal quantityTaken =
        procurement == null ? BigDecimal.ZERO : procurement.getQuantityTaken();

    return new FarmerDashboardBatchDetailResponse(
        BatchResponse.from(batch),
        FarmResponse.from(farm),
        traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId).stream()
            .map(TraceEventResponse::from)
            .toList(),
        priceBreakdownRepository
            .findByBatchId(batchId)
            .map(PriceBreakdownResponse::from)
            .orElse(null),
        procurement == null ? null : BatchProcurementResponse.from(procurement),
        sales.stream().map(BatchSaleTransactionResponse::from).toList(),
        totalQuantitySold,
        quantityTaken.subtract(totalQuantitySold),
        sumSaleAmount(sales));
  }

  private BigDecimal sumQuantitySold(List<BatchSaleTransaction> sales) {
    return sales.stream()
        .map(BatchSaleTransaction::getQuantitySold)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal sumSaleAmount(List<BatchSaleTransaction> sales) {
    return sales.stream()
        .map(BatchSaleTransaction::getSaleAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Farmer resolveDashboardFarmer(UUID requestedFarmerId) {
    User user = currentUserService.getCurrentUser();
    if (UserRole.FARMER.equals(user.getRole())) {
      // Farmer callers are always resolved from their login, never from client-supplied IDs.
      return farmerRepository
          .findByUserId(user.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Farmer profile is not linked"));
    }
    if (requestedFarmerId == null) {
      throw new BadRequestException("farmerId is required for ADMIN dashboard access");
    }
    return farmerRepository
        .findById(requestedFarmerId)
        .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
  }

  private void enforceOwnership(Batch batch) {
    User user = currentUserService.getCurrentUser();
    if (!UserRole.FARMER.equals(user.getRole())) return;
    Farmer farmer =
        farmerRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Farmer profile is not linked"));
    if (!farmer.getId().equals(batch.getFarmerId())) {
      throw new AccessDeniedException("You cannot access another farmer's batch");
    }
  }
}
