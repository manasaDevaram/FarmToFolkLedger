package com.farmtofolk.farmtofolk_ledger.batch;

import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BatchService {

    private final BatchRepository batchRepository;
    private final FarmRepository farmRepository;
    private final FarmerRepository farmerRepository;

    public BatchService(
            BatchRepository batchRepository,
            FarmRepository farmRepository,
            FarmerRepository farmerRepository
    ) {
        this.batchRepository = batchRepository;
        this.farmRepository = farmRepository;
        this.farmerRepository = farmerRepository;
    }

    public BatchResponse createBatch(CreateBatchRequest request) {
        // Make sure the batch points to a real farmer.
        verifyFarmerExists(request.farmerId());
        // Make sure the farm belongs to that farmer.
        verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

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

    public List<BatchResponse> getBatchesByFarmer(UUID farmerId) {
        // Make sure the farmer exists before listing their batches.
        verifyFarmerExists(farmerId);

        // Fetch all batches for this farmer and convert each one to a response.
        return batchRepository.findByFarmerId(farmerId)
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    public List<BatchResponse> getBatchesByFarm(UUID farmId) {
        // Make sure the farm exists before listing its batches.
        verifyFarmExists(farmId);

        // Fetch all batches for this farm and convert each one to a response.
        return batchRepository.findByFarmId(farmId)
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    public BatchResponse updateBatch(UUID batchId, CreateBatchRequest request) {
        // Make sure the updated batch still points to a real farmer.
        verifyFarmerExists(request.farmerId());
        // Make sure the updated farm belongs to that farmer.
        verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

        // Load the existing batch, update its fields, then save it.
        Batch batch = findBatch(batchId);
        applyRequest(batch, request);

        Batch savedBatch = batchRepository.save(batch);
        return BatchResponse.from(savedBatch);
    }

    private Batch findBatch(UUID batchId) {
        // Reuse one not-found lookup rule for all batch reads and updates.
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
    }

    private void verifyFarmerExists(UUID farmerId) {
        // Prevent creating or listing batches for farmers that do not exist.
        if (!farmerRepository.existsById(farmerId)) {
            throw new RuntimeException("Farmer not found");
        }
    }

    private void verifyFarmExists(UUID farmId) {
        // Prevent listing batches for farms that do not exist.
        if (!farmRepository.existsById(farmId)) {
            throw new RuntimeException("Farm not found");
        }
    }

    private void verifyFarmBelongsToFarmer(UUID farmId, UUID farmerId) {
        // Load the farm so we can validate ownership against the farmer ID.
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        // Prevent batches from linking one farmer to another farmer's farm.
        if (!farm.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("Farm does not belong to farmer");
        }
    }

    private void applyRequest(Batch batch, CreateBatchRequest request) {
        // Keep request-to-entity field mapping in one place.
        batch.setBatchCode(request.batchCode());
        batch.setFarmId(request.farmId());
        batch.setFarmerId(request.farmerId());
        batch.setCropName(request.cropName());
        batch.setVariety(request.variety());
        batch.setQuantity(request.quantity());
        batch.setUnit(request.unit());
        batch.setHarvestDate(request.harvestDate());
        batch.setPackedDate(request.packedDate());
        batch.setBestBeforeDate(request.bestBeforeDate());
        batch.setStatus(request.status());
    }
}
