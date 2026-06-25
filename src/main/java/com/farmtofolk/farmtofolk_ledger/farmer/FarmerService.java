package com.farmtofolk.farmtofolk_ledger.farmer;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class FarmerService {

    private static final Set<String> PROFILE_PHOTO_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> INTRO_VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/quicktime"
    );

    private final FarmerRepository farmerRepository;
    private final PublicTraceCacheService publicTraceCacheService;
    private final StorageService storageService;

    public FarmerService(
            FarmerRepository farmerRepository,
            PublicTraceCacheService publicTraceCacheService,
            StorageService storageService
    ) {
        this.farmerRepository = farmerRepository;
        this.publicTraceCacheService = publicTraceCacheService;
        this.storageService = storageService;
    }

    public FarmerResponse createFarmer(CreateFarmerRequest request) {
        // Copy request data into a new Farmer entity.
        Farmer farmer = new Farmer();
        applyRequest(farmer, request);
        if (farmer.getFarmerCode() == null || farmer.getFarmerCode().isBlank()) {
            farmer.setFarmerCode(generateFarmerCode());
        }

        // Save the farmer and return API-friendly response data.
        Farmer savedFarmer = farmerRepository.save(farmer);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse uploadProfilePhoto(UUID farmerId, MultipartFile file) {
        // Store the photo in S3 and save the resulting URL on the farmer profile.
        Farmer farmer = findFarmer(farmerId);
        StoredFileResponse storedFile = storageService.upload(
                file,
                "farmers/" + farmerId + "/profile-photo",
                PROFILE_PHOTO_CONTENT_TYPES
        );
        farmer.setProfilePhotoUrl(storedFile.fileUrl());

        Farmer savedFarmer = farmerRepository.save(farmer);
        publicTraceCacheService.evictStableDataForFarmer(farmerId);
        return FarmerResponse.from(savedFarmer);
    }

    public FarmerResponse uploadIntroVideo(UUID farmerId, MultipartFile file) {
        // Store the intro video in S3 and save the resulting URL on the farmer profile.
        Farmer farmer = findFarmer(farmerId);
        StoredFileResponse storedFile = storageService.upload(
                file,
                "farmers/" + farmerId + "/intro-video",
                INTRO_VIDEO_CONTENT_TYPES
        );
        farmer.setIntroVideoUrl(storedFile.fileUrl());

        Farmer savedFarmer = farmerRepository.save(farmer);
        publicTraceCacheService.evictStableDataForFarmer(farmerId);
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
        if (request.farmerCode() != null && !request.farmerCode().isBlank()) {
            farmer.setFarmerCode(request.farmerCode());
        }
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

    private String generateFarmerCode() {
        int year = java.time.LocalDate.now().getYear();
        long sequence = farmerRepository.count() + 1;
        String farmerCode = formatFarmerCode(year, sequence);

        while (farmerRepository.existsByFarmerCode(farmerCode)) {
            sequence++;
            farmerCode = formatFarmerCode(year, sequence);
        }

        return farmerCode;
    }

    private String formatFarmerCode(int year, long sequence) {
        return "FTF-FR-" + year + "-" + String.format("%06d", sequence);
    }
}
