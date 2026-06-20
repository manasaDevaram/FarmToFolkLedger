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
        verifyFarmerExists(request.farmerId());
        verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

        Batch batch = new Batch();
        applyRequest(batch, request);

        Batch savedBatch = batchRepository.save(batch);
        return BatchResponse.from(savedBatch);
    }

    public BatchResponse getBatch(UUID batchId) {
        Batch batch = findBatch(batchId);
        return BatchResponse.from(batch);
    }

    public List<BatchResponse> getBatchesByFarmer(UUID farmerId) {
        verifyFarmerExists(farmerId);

        return batchRepository.findByFarmerId(farmerId)
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    public List<BatchResponse> getBatchesByFarm(UUID farmId) {
        verifyFarmExists(farmId);

        return batchRepository.findByFarmId(farmId)
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    public BatchResponse updateBatch(UUID batchId, CreateBatchRequest request) {
        verifyFarmerExists(request.farmerId());
        verifyFarmBelongsToFarmer(request.farmId(), request.farmerId());

        Batch batch = findBatch(batchId);
        applyRequest(batch, request);

        Batch savedBatch = batchRepository.save(batch);
        return BatchResponse.from(savedBatch);
    }

    private Batch findBatch(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
    }

    private void verifyFarmerExists(UUID farmerId) {
        if (!farmerRepository.existsById(farmerId)) {
            throw new RuntimeException("Farmer not found");
        }
    }

    private void verifyFarmExists(UUID farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new RuntimeException("Farm not found");
        }
    }

    private void verifyFarmBelongsToFarmer(UUID farmId, UUID farmerId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        if (!farm.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("Farm does not belong to farmer");
        }
    }

    private void applyRequest(Batch batch, CreateBatchRequest request) {
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
