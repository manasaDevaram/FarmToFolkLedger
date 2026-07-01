package com.farmtofolk.farmtofolk_ledger.batch;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.util.List;
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
  private final PublicTraceCacheService publicTraceCacheService;
  private final AfterCommitExecutor afterCommitExecutor;

  public BatchService(
      BatchRepository batchRepository,
      FarmRepository farmRepository,
      FarmerRepository farmerRepository,
      PublicTraceCacheService publicTraceCacheService,
      AfterCommitExecutor afterCommitExecutor) {
    this.batchRepository = batchRepository;
    this.farmRepository = farmRepository;
    this.farmerRepository = farmerRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.afterCommitExecutor = afterCommitExecutor;
  }

  public BatchResponse createBatch(CreateBatchRequest request) {
    // Make sure the batch points to a real farmer.
    verifyFarmerExists(request.farmerId());
    // Make sure the farm belongs to that farmer.
    verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());
    validateUniqueBatchCode(request.batchCode(), null);

    // Copy request data into a new Batch entity.
    Batch batch = new Batch();
    applyRequest(batch, request);

    // Save the batch and return API-friendly response data.
    Batch savedBatch = batchRepository.save(batch);
    return BatchResponse.from(savedBatch);
  }

  public BatchResponse getBatch(UUID batchId) {
    // Load one batch by ID and convert it to a response.
    Batch batch = findBatch(batchId);
    return BatchResponse.from(batch);
  }

  public List<BatchListResponse> getAllBatches(
      UUID farmerId, UUID farmId, String cropName, String status) {
    // Fetch batches for admin lists and apply simple optional filters.
    List<Batch> batches =
        batchRepository.findAll().stream()
            .filter(batch -> farmerId == null || farmerId.equals(batch.getFarmerId()))
            .filter(batch -> farmId == null || farmId.equals(batch.getFarmId()))
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

  public List<BatchResponse> getBatchesByFarmer(UUID farmerId) {
    // Make sure the farmer exists before listing their batches.
    verifyFarmerExists(farmerId);

    // Fetch all batches for this farmer and convert each one to a response.
    return batchRepository.findByFarmerId(farmerId).stream().map(BatchResponse::from).toList();
  }

  public List<BatchResponse> getBatchesByFarm(UUID farmId) {
    // Make sure the farm exists before listing its batches.
    verifyFarmExists(farmId);

    // Fetch all batches for this farm and convert each one to a response.
    return batchRepository.findByFarmId(farmId).stream().map(BatchResponse::from).toList();
  }

  public BatchResponse updateBatch(UUID batchId, CreateBatchRequest request) {
    // Make sure the updated batch still points to a real farmer.
    verifyFarmerExists(request.farmerId());
    // Make sure the updated farm belongs to that farmer.
    verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());
    validateUniqueBatchCode(request.batchCode(), batchId);

    // Load the existing batch, update its fields, then save it.
    Batch batch = findBatch(batchId);
    applyRequest(batch, request);

    Batch savedBatch = batchRepository.save(batch);
    // Clear QR page stable data because batch details changed.
    afterCommitExecutor.run(() -> publicTraceCacheService.evictStableDataForBatch(batchId));
    return BatchResponse.from(savedBatch);
  }

  private Batch findBatch(UUID batchId) {
    // Reuse one not-found lookup rule for all batch reads and updates.
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
  }

  private void verifyFarmerExists(UUID farmerId) {
    // Prevent creating or listing batches for farmers that do not exist.
    if (!farmerRepository.existsById(farmerId)) {
      throw new ResourceNotFoundException("Farmer not found");
    }
  }

  private void verifyFarmExists(UUID farmId) {
    // Prevent listing batches for farms that do not exist.
    if (!farmRepository.existsById(farmId)) {
      throw new ResourceNotFoundException("Farm not found");
    }
  }

  private void verifyFarmBelongsToFarmer(UUID farmId, UUID farmerId) {
    // Load the farm so we can validate ownership against the farmer ID.
    Farm farm =
        farmRepository
            .findById(farmId)
            .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

    // Prevent batches from linking one farmer to another farmer's farm.
    if (!farm.getFarmerId().equals(farmerId)) {
      throw new BadRequestException("Farm does not belong to farmer");
    }
  }

  private void applyRequest(Batch batch, CreateBatchRequest request) {
    // Keep request-to-entity field mapping in one place.
    batch.setBatchCode(request.batchCode().trim());
    batch.setFarmId(request.farmId());
    batch.setFarmerId(request.farmerId());
    batch.setCropName(request.cropName());
    batch.setVariety(request.variety());
    batch.setQuantity(request.quantity());
    batch.setUnit(request.unit());
    batch.setHarvestDate(request.harvestDate());
    batch.setStatus(request.status());
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

  private void validateUniqueBatchCode(String batchCode, UUID batchId) {
    String normalizedBatchCode = batchCode.trim();
    boolean duplicate =
        batchId == null
            ? batchRepository.existsByBatchCode(normalizedBatchCode)
            : batchRepository.existsByBatchCodeAndIdNot(normalizedBatchCode, batchId);
    if (duplicate) throw new ConflictException("Batch code already exists");
  }
}
