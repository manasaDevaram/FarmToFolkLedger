package com.farmtofolk.farmtofolk_ledger.farmer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public FarmerResponse createFarmer(CreateFarmerRequest request) {
        Farmer farmer = new Farmer();
        applyRequest(farmer, request);

        Farmer savedFarmer = farmerRepository.save(farmer);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse getFarmer(UUID farmerId) {
        Farmer farmer = findFarmer(farmerId);
        return FarmerResponse.from(farmer);
    }

    public List<FarmerResponse> getAllFarmers() {
        return farmerRepository.findAll()
                .stream()
                .map(FarmerResponse::from)
                .toList();
    }

    public FarmerResponse updateFarmer(UUID farmerId, CreateFarmerRequest request) {
        Farmer farmer = findFarmer(farmerId);
        applyRequest(farmer, request);

        Farmer savedFarmer = farmerRepository.save(farmer);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse updateFarmerStatus(UUID farmerId, UpdateFarmerStatusRequest request) {
        Farmer farmer = findFarmer(farmerId);
        farmer.setActive(request.active());

        Farmer savedFarmer = farmerRepository.save(farmer);
        return FarmerResponse.from(savedFarmer);
    }

    private Farmer findFarmer(UUID farmerId) {
        return farmerRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
    }

    private void applyRequest(Farmer farmer, CreateFarmerRequest request) {
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
