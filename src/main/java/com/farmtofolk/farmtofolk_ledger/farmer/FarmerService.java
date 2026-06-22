package com.farmtofolk.farmtofolk_ledger.farmer;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final PublicTraceCacheService publicTraceCacheService;

    public FarmerService(
            FarmerRepository farmerRepository,
            PublicTraceCacheService publicTraceCacheService
    ) {
        this.farmerRepository = farmerRepository;
        this.publicTraceCacheService = publicTraceCacheService;
    }

    public FarmerResponse createFarmer(CreateFarmerRequest request) {
        // Copy request data into a new Farmer entity.
        Farmer farmer = new Farmer();
        applyRequest(farmer, request);

        // Save the farmer and return API-friendly response data.
        Farmer savedFarmer = farmerRepository.save(farmer);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse getFarmer(UUID farmerId) {
        // Load one farmer by ID and convert it to a response.
        Farmer farmer = findFarmer(farmerId);
        return FarmerResponse.from(farmer);
    }

    public List<FarmerResponse> getAllFarmers() {
        // Fetch all farmers and convert each one to a response.
        return farmerRepository.findAll()
                .stream()
                .map(FarmerResponse::from)
                .toList();
    }

    public FarmerResponse updateFarmer(UUID farmerId, CreateFarmerRequest request) {
        // Load the existing farmer, update its fields, then save it.
        Farmer farmer = findFarmer(farmerId);
        applyRequest(farmer, request);

        Farmer savedFarmer = farmerRepository.save(farmer);
        // Clear QR page stable data because farmer details changed.
        publicTraceCacheService.evictStableDataForFarmer(farmerId);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse updateFarmerStatus(UUID farmerId, UpdateFarmerStatusRequest request) {
        // Load the farmer and update only the active status.
        Farmer farmer = findFarmer(farmerId);
        farmer.setActive(request.active());

        Farmer savedFarmer = farmerRepository.save(farmer);
        // Clear QR page stable data because farmer status changed.
        publicTraceCacheService.evictStableDataForFarmer(farmerId);
        return FarmerResponse.from(savedFarmer);
    }

    private Farmer findFarmer(UUID farmerId) {
        // Reuse one not-found lookup rule for all farmer reads and updates.
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
    }

    private void applyRequest(Farmer farmer, CreateFarmerRequest request) {
        // Keep request-to-entity field mapping in one place.
        farmer.setFarmerCode(request.farmerCode());
        farmer.setName(request.name());
        farmer.setPhone(request.phone());
        farmer.setVillage(request.village());
        farmer.setDistrict(request.district());
        farmer.setState(request.state());
        farmer.setBio(request.bio());
        farmer.setProfilePhotoUrl(request.profilePhotoUrl());
        farmer.setIntroVideoUrl(request.introVideoUrl());
        farmer.setJoinedDate(request.joinedDate());
    }
}
