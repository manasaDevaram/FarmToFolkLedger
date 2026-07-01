package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.auth.User;
import com.farmtofolk.farmtofolk_ledger.auth.UserRole;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEvent;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsage;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageRepository;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
  private final FarmerRepository farmerRepository;
  private final FarmRepository farmRepository;
  private final BatchRepository batchRepository;
  private final TraceEventRepository traceEventRepository;
  private final BatchUsageRepository batchUsageRepository;
  private final StorageService storageService;

  public FarmerDashboardService(
      CurrentUserService currentUserService,
      FarmerRepository farmerRepository,
      FarmRepository farmRepository,
      BatchRepository batchRepository,
      TraceEventRepository traceEventRepository,
      BatchUsageRepository batchUsageRepository,
      StorageService storageService) {
    this.currentUserService = currentUserService;
    this.farmerRepository = farmerRepository;
    this.farmRepository = farmRepository;
    this.batchRepository = batchRepository;
    this.traceEventRepository = traceEventRepository;
    this.batchUsageRepository = batchUsageRepository;
    this.storageService = storageService;
  }

  public FarmerDashboardSummaryResponse getSummary(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    List<Farm> farms = farmRepository.findByFarmerId(farmer.getId());
    List<Batch> batches = batchRepository.findByFarmerId(farmer.getId());
    Map<UUID, BatchMetrics> metricsByBatch = loadMetrics(batches);
    Map<UUID, List<Batch>> batchesByFarm =
        batches.stream().collect(Collectors.groupingBy(Batch::getFarmId));

    List<FarmerDashboardFarmResponse> farmResponses =
        farms.stream()
            .map(
                farm ->
                    new FarmerDashboardFarmResponse(
                        FarmResponse.from(farm),
                        batchesByFarm.getOrDefault(farm.getId(), List.of()).stream()
                            .sorted(dashboardBatchComparator(metricsByBatch))
                            .map(batch -> toWorkBatchResponse(batch, metricsByBatch.get(batch.getId())))
                            .toList()))
            .toList();

    return new FarmerDashboardSummaryResponse(FarmerResponse.from(farmer, storageService), farmResponses);
  }

  public List<FarmResponse> getFarms(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    return farmRepository.findByFarmerId(farmer.getId()).stream().map(FarmResponse::from).toList();
  }

  public List<FarmerDashboardBatchResponse> getBatches(UUID requestedFarmerId) {
    Farmer farmer = resolveDashboardFarmer(requestedFarmerId);
    List<Batch> batches = batchRepository.findByFarmerId(farmer.getId());
    Map<UUID, BatchMetrics> metricsByBatch = loadMetrics(batches);
    Map<UUID, Farm> farms =
        farmRepository
            .findAllById(batches.stream().map(Batch::getFarmId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farm::getId, Function.identity()));

    return batches.stream()
        .map(batch -> toLegacyBatchResponse(batch, farms.get(batch.getFarmId()), metricsByBatch.get(batch.getId())))
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

    return new FarmerDashboardBatchDetailResponse(
        BatchResponse.from(batch),
        FarmResponse.from(farm),
        traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId).stream()
            .map(TraceEventResponse::from)
            .toList(),
        batchUsageRepository.findByBatchIdOrderByRecordedAtAsc(batchId).stream()
            .map(BatchUsageResponse::from).toList());
  }

  private Map<UUID, BatchMetrics> loadMetrics(List<Batch> batches) {
    if (batches.isEmpty()) return Map.of();

    List<UUID> batchIds = batches.stream().map(Batch::getId).toList();
    Map<UUID, List<BatchUsage>> usages =
        batchUsageRepository.findByBatchIdInOrderByRecordedAtAsc(batchIds).stream()
            .collect(Collectors.groupingBy(BatchUsage::getBatchId));
    Map<UUID, List<TraceEvent>> traces =
        traceEventRepository.findByBatchIdInOrderByEventTimeAsc(batchIds).stream()
            .collect(Collectors.groupingBy(TraceEvent::getBatchId));

    Map<UUID, BatchMetrics> result = new HashMap<>();
    for (Batch batch : batches) {
      List<BatchUsage> batchUsages = usages.getOrDefault(batch.getId(), List.of());
      List<TraceEvent> batchTraces = traces.getOrDefault(batch.getId(), List.of());
      result.put(
          batch.getId(),
          new BatchMetrics(
              batchUsages,
              batchTraces.isEmpty() ? null : batchTraces.getLast(),
              sumUsageSaleAmount(batchUsages)));
    }
    return result;
  }

  private FarmerDashboardWorkBatchResponse toWorkBatchResponse(
      Batch batch, BatchMetrics metrics) {
    BigDecimal produced = zeroIfNull(batch.getQuantityReceived());
    BigDecimal sold = zeroIfNull(batch.getQuantitySold());
    BigDecimal remaining = zeroIfNull(batch.getQuantityAvailable());

    return new FarmerDashboardWorkBatchResponse(
        batch.getId(),
        batch.getBatchCode(),
        batch.getCropName(),
        metrics.latestTrace() == null ? null : metrics.latestTrace().getEventType(),
        batch.getStatus(),
        batch.getHarvestDate(),
        produced,
        sold,
        remaining,
        batch.getFarmerPricePerUnit(),
        batch.getConsumerPricePerUnit(),
        batch.getTotalFarmerAmount(),
        batch.getPaymentStatus(),
        metrics.totalSaleAmount(),
        lastUpdated(batch, metrics));
  }

  private FarmerDashboardBatchResponse toLegacyBatchResponse(
      Batch batch, Farm farm, BatchMetrics metrics) {
    BigDecimal quantityTaken = zeroIfNull(batch.getQuantityReceived());
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
        metrics.latestTrace() == null ? null : metrics.latestTrace().getEventType(),
        batch.getFarmerPricePerUnit(),
        batch.getTotalFarmerAmount(),
        batch.getPaymentStatus(),
        zeroIfNull(batch.getQuantitySold()),
        zeroIfNull(batch.getQuantityAvailable()),
        metrics.totalSaleAmount(),
        "INR");
  }

  private LocalDateTime lastUpdated(Batch batch, BatchMetrics metrics) {
    return latest(
        Arrays.asList(
            batch.getUpdatedAt(),
            metrics.latestTrace() == null ? null : metrics.latestTrace().getCreatedAt(),
            metrics.usages().stream()
                .map(BatchUsage::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null)));
  }

  private Comparator<Batch> dashboardBatchComparator(Map<UUID, BatchMetrics> metricsByBatch) {
    return Comparator.<Batch, LocalDateTime>comparing(
            batch -> lastUpdated(batch, metricsByBatch.get(batch.getId())),
            Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(
            Batch::getHarvestDate, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(
            Batch::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(Batch::getId, Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private LocalDateTime latest(Collection<LocalDateTime> values) {
    return values.stream().filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
  }

  private BigDecimal sumUsageSaleAmount(List<BatchUsage> usages) {
    return usages.stream()
        .filter(usage -> usage.getUsageType().isSale() && usage.getPricePerUnit() != null)
        .map(usage -> usage.getQuantity().multiply(usage.getPricePerUnit()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private Farmer resolveDashboardFarmer(UUID requestedFarmerId) {
    User user = currentUserService.getCurrentUser();
    if (UserRole.FARMER.equals(user.getRole())) {
      // Farmer identity always comes from the JWT-linked profile, never a request parameter.
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

  private record BatchMetrics(
      List<BatchUsage> usages,
      TraceEvent latestTrace,
      BigDecimal totalSaleAmount) {}
}
