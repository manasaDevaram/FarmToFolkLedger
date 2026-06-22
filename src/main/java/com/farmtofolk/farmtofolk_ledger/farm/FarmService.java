package com.farmtofolk.farmtofolk_ledger.farm;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmService {

    private final FarmRepository farmRepository;
    private final FarmerRepository farmerRepository;
    private final PublicTraceCacheService publicTraceCacheService;

    public FarmService(
            FarmRepository farmRepository,
            FarmerRepository farmerRepository,
            PublicTraceCacheService publicTraceCacheService
    ) {
        this.farmRepository = farmRepository;
        this.farmerRepository = farmerRepository;
        this.publicTraceCacheService = publicTraceCacheService;
    }

    public FarmResponse createFarm(CreateFarmRequest request) {
        // Make sure the farm is linked to a real farmer.
        verifyFarmerExists(request.farmerId());

        // Copy request data into a new Farm entity.
        Farm farm = new Farm();
        applyRequest(farm, request);

        // Save the farm and return API-friendly response data.
        Farm savedFarm = farmRepository.save(farm);
        return FarmResponse.from(savedFarm);
    }

    public FarmResponse getFarm(UUID farmId) {
        // Load one farm by ID and convert it to a response.
        Farm farm = findFarm(farmId);
        return FarmResponse.from(farm);
    }

    public List<FarmResponse> getFarmsByFarmer(UUID farmerId) {
        // Make sure the farmer exists before listing their farms.
        verifyFarmerExists(farmerId);

        // Fetch all farms for this farmer and convert each one to a response.
        return farmRepository.findByFarmerId(farmerId)
                .stream()
                .map(FarmResponse::from)
                .toList();
    }

    public FarmResponse updateFarm(UUID farmId, CreateFarmRequest request) {
        // Make sure the updated farm still points to a real farmer.
        verifyFarmerExists(request.farmerId());

        // Load the existing farm, update its fields, then save it.
        Farm farm = findFarm(farmId);
        applyRequest(farm, request);

        Farm savedFarm = farmRepository.save(farm);
        // Clear QR page stable data because farm details changed.
        publicTraceCacheService.evictStableDataForFarm(farmId);
        return FarmResponse.from(savedFarm);
    }

    private Farm findFarm(UUID farmId) {
        // Reuse one not-found lookup rule for all farm reads and updates.
        return farmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));
    }

    private void verifyFarmerExists(UUID farmerId) {
        // Prevent creating or updating farms for farmers that do not exist.
        if (!farmerRepository.existsById(farmerId)) {
            throw new ResourceNotFoundException("Farmer not found");
        }
    }

    private void applyRequest(Farm farm, CreateFarmRequest request) {
        // Keep request-to-entity field mapping in one place.
        farm.setFarmerId(request.farmerId());
        farm.setFarmName(request.farmName());
        farm.setVillage(request.village());
        farm.setDistrict(request.district());
        farm.setState(request.state());
        farm.setLatitude(request.latitude());
        farm.setLongitude(request.longitude());
        farm.setSizeAcres(request.sizeAcres());
        farm.setFarmingType(request.farmingType());
    }
}
