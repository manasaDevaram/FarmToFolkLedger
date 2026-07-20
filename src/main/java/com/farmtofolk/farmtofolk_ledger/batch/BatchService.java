package com.farmtofolk.farmtofolk_ledger.batch;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.BatchUpdatedEvent;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeService;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BatchService {

  private final BatchRepository batchRepository;
  private final FarmRepository farmRepository;
  private final FarmerRepository farmerRepository;
  private final DomainEventPublisher domainEventPublisher;
  private final QrCodeService qrCodeService;

  public BatchService(
      BatchRepository batchRepository,
      FarmRepository farmRepository,
      FarmerRepository farmerRepository,
      DomainEventPublisher domainEventPublisher,
      QrCodeService qrCodeService) {
    this.batchRepository = batchRepository;
    this.farmRepository = farmRepository;
    this.farmerRepository = farmerRepository;
    this.domainEventPublisher = domainEventPublisher;
    this.qrCodeService = qrCodeService;
  }

  public BatchResponse createSowingBatch(CreateSowingBatchRequest request) {
    verifyFarmerExists(request.farmerId());
    verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

    Batch batch = new Batch();
    batch.setBatchCode(generateBatchCode());
    batch.setBatchType(BatchType.SOWING);
    batch.setFarmId(request.farmId());
    batch.setFarmerId(request.farmerId());
    batch.setCropName(request.cropName());
    batch.setVariety(request.variety());
    batch.setAcresSown(request.acresSown());
    batch.setSowingDate(request.sowingDate());
    batch.setUnit("acres");
    batch.setQuantityReceived(BigDecimal.ZERO);
    batch.setHarvestDate(null);
    batch.setReceivedDate(null);
    batch.setFarmerPricePerUnit(BigDecimal.ZERO);
    batch.setPaymentStatus(PaymentStatus.UNPAID);
    batch.setStatus("SOWN");
    batch.setConsumerPricePerUnit(BigDecimal.ZERO);
    batch.setOperationalCostPerUnit(BigDecimal.ZERO);
    batch.initializeInventory();

    Batch savedBatch = batchRepository.saveAndFlush(batch);
    qrCodeService.createQrCodeForSowing(savedBatch.getId());
    return BatchResponse.from(savedBatch);
  }

  public BatchResponse createProcuredBatch(CreateProcuredBatchRequest request) {
    verifyFarmerExists(request.farmerId());
    verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

    Batch sowingBatch = findBatch(request.parentBatchId());
    if (sowingBatch.getBatchType() != BatchType.SOWING) {
      throw new BadRequestException("Parent batch must be a sowing batch");
    }
    if (!sowingBatch.getFarmId().equals(request.farmId())) {
      throw new BadRequestException("Procured batch farm must match sowing batch farm");
    }
    if (!sowingBatch.getFarmerId().equals(request.farmerId())) {
      throw new BadRequestException("Procured batch farmer must match sowing batch farmer");
    }

    Batch batch = new Batch();
    batch.setBatchCode(generateBatchCode());
    batch.setBatchType(BatchType.PROCURED);
    batch.setParentBatchId(request.parentBatchId());
    batch.setFarmId(request.farmId());
    batch.setFarmerId(request.farmerId());
    batch.setCropName(request.cropName());
    batch.setVariety(request.variety());
    batch.setQuantityReceived(request.quantityReceived());
    batch.setUnit(request.unit());
    batch.setHarvestDate(request.harvestDate());
    batch.setReceivedDate(request.receivedDate());
    batch.setFarmerPricePerUnit(request.farmerPricePerUnit());
    batch.setPaymentStatus(request.paymentStatus());
    batch.setStatus("RECEIVED");
    batch.setConsumerPricePerUnit(BigDecimal.ZERO);
    batch.setOperationalCostPerUnit(BigDecimal.ZERO);
    batch.initializeInventory();

    Batch savedBatch = batchRepository.saveAndFlush(batch);
    qrCodeService.pointQrToProcuredBatch(request.parentBatchId(), savedBatch.getId());
    return BatchResponse.from(savedBatch);
  }

  public BatchResponse createBatch(CreateBatchRequest request) {
    throw new BadRequestException(
        "Use POST /api/batches/sowing or POST /api/batches/procured instead");
  }

  public BatchResponse getBatch(UUID batchId) {
    Batch batch = findBatch(batchId);
    return BatchResponse.from(batch);
  }

  public List<BatchListResponse> getAllBatches(
      UUID farmerId, UUID farmId, String cropName, String status, BatchType batchType) {
    List<Batch> batches =
        batchRepository.findAll().stream()
            .filter(batch -> farmerId == null || farmerId.equals(batch.getFarmerId()))
            .filter(batch -> farmId == null || farmId.equals(batch.getFarmId()))
            .filter(batch -> batchType == null || batchType.equals(batch.getBatchType()))
            .filter(batch -> matches(batch.getCropName(), cropName))
            .filter(batch -> matches(batch.getStatus(), status))
            .toList();

    Map<UUID, Farmer> farmersById =
        farmerRepository
            .findAllById(
                batches.stream()
                    .map(Batch::getFarmerId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farmer::getId, Function.identity()));
    Map<UUID, Farm> farmsById =
        farmRepository
            .findAllById(
                batches.stream()
                    .map(Batch::getFarmId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farm::getId, Function.identity()));

    return batches.stream()
        .map(
            batch ->
                BatchListResponse.from(
                    batch,
                    farmerName(batch.getFarmerId(), farmersById),
                    farmName(batch.getFarmId(), farmsById)))
        .toList();
  }

  public List<BatchResponse> getSowingBatchesByFarm(UUID farmId) {
    verifyFarmExists(farmId);
    return batchRepository.findByFarmIdAndBatchType(farmId, BatchType.SOWING).stream()
        .filter(batch -> !"CLOSED".equalsIgnoreCase(batch.getStatus()))
        .map(BatchResponse::from)
        .toList();
  }

  public List<BatchResponse> getBatchesByFarmer(UUID farmerId) {
    verifyFarmerExists(farmerId);
    return batchRepository.findByFarmerId(farmerId).stream().map(BatchResponse::from).toList();
  }

  public List<BatchResponse> getBatchesByFarm(UUID farmId) {
    verifyFarmExists(farmId);
    return batchRepository.findByFarmId(farmId).stream().map(BatchResponse::from).toList();
  }

  public List<BatchResponse> getProcuredBatchesForSowing(UUID sowingBatchId) {
    Batch sowingBatch = findBatch(sowingBatchId);
    if (sowingBatch.getBatchType() != BatchType.SOWING) {
      throw new BadRequestException("Batch is not a sowing batch");
    }
    return batchRepository.findByParentBatchIdOrderByReceivedDateDesc(sowingBatchId).stream()
        .map(BatchResponse::from)
        .toList();
  }

  public BatchResponse updateBatch(UUID batchId, CreateBatchRequest request) {
    Batch batch = findBatch(batchId);
    if (batch.getBatchType() != BatchType.PROCURED) {
      throw new BadRequestException("Only procured batches can be updated with this endpoint");
    }

    verifyFarmerExists(request.farmerId());
    verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

    String existingBatchCode = batch.getBatchCode();
    BigDecimal existingConsumerPrice = batch.getConsumerPricePerUnit();
    BigDecimal existingOperationalCost = batch.getOperationalCostPerUnit();
    applyProcuredRequest(batch, request);
    batch.setBatchCode(existingBatchCode);
    batch.setConsumerPricePerUnit(existingConsumerPrice);
    batch.setOperationalCostPerUnit(existingOperationalCost);
    BigDecimal committedQuantity =
        zero(batch.getQuantitySold())
            .add(zero(batch.getQuantityWasted()))
            .add(zero(batch.getQuantityUsedInProduct()));
    if (request.quantityReceived().compareTo(committedQuantity) < 0) {
      throw new BadRequestException("quantityReceived cannot be less than already used quantity");
    }
    batch.setQuantityAvailable(request.quantityReceived().subtract(committedQuantity));
    batch.calculateTotalFarmerAmount();

    Batch savedBatch = batchRepository.save(batch);
    domainEventPublisher.publishAfterCommit(new BatchUpdatedEvent(batchId));
    return BatchResponse.from(savedBatch);
  }

  private Batch findBatch(UUID batchId) {
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
  }

  private void verifyFarmerExists(UUID farmerId) {
    if (!farmerRepository.existsById(farmerId)) {
      throw new ResourceNotFoundException("Farmer not found");
    }
  }

  private void verifyFarmExists(UUID farmId) {
    if (!farmRepository.existsById(farmId)) {
      throw new ResourceNotFoundException("Farm not found");
    }
  }

  private void verifyFarmBelongsToFarmer(UUID farmId, UUID farmerId) {
    Farm farm =
        farmRepository
            .findById(farmId)
            .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

    if (!farm.getFarmerId().equals(farmerId)) {
      throw new BadRequestException("Farm does not belong to farmer");
    }

    if (!Boolean.TRUE.equals(farm.getActive())) {
      throw new BadRequestException("Farm is inactive");
    }
  }

  private void applyProcuredRequest(Batch batch, CreateBatchRequest request) {
    batch.setFarmId(request.farmId());
    batch.setFarmerId(request.farmerId());
    batch.setCropName(request.cropName());
    batch.setVariety(request.variety());
    batch.setQuantityReceived(request.quantityReceived());
    batch.setUnit(request.unit());
    batch.setHarvestDate(request.harvestDate());
    batch.setReceivedDate(request.receivedDate());
    batch.setFarmerPricePerUnit(request.farmerPricePerUnit());
    batch.setPaymentStatus(request.paymentStatus());
    if (request.status() != null && !request.status().isBlank()) {
      batch.setStatus(request.status());
    }
  }

  private boolean matches(String actual, String expected) {
    return expected == null
        || expected.isBlank()
        || (actual != null && actual.equalsIgnoreCase(expected));
  }

  private String farmerName(UUID farmerId, Map<UUID, Farmer> farmersById) {
    Farmer farmer = farmersById.get(farmerId);
    return farmer == null ? null : farmer.getName();
  }

  private String farmName(UUID farmId, Map<UUID, Farm> farmsById) {
    Farm farm = farmsById.get(farmId);
    return farm == null ? null : farm.getFarmName();
  }

  private String generateBatchCode() {
    String prefix = "FTF-BATCH-" + java.time.LocalDate.now().getYear() + "-";
    long sequence = batchRepository.count() + 1;
    String generated = prefix + String.format("%06d", sequence);
    while (batchRepository.existsByBatchCode(generated)) {
      generated = prefix + String.format("%06d", ++sequence);
    }
    return generated;
  }

  private BigDecimal zero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
