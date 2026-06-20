package com.farmtofolk.farmtofolk_ledger.verification;

import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmVerificationService {

    private final FarmVerificationRepository farmVerificationRepository;
    private final FarmRepository farmRepository;

    public FarmVerificationService(
            FarmVerificationRepository farmVerificationRepository,
            FarmRepository farmRepository
    ) {
        this.farmVerificationRepository = farmVerificationRepository;
        this.farmRepository = farmRepository;
    }

    public FarmVerificationResponse createFarmVerification(
            UUID farmId,
            CreateFarmVerificationRequest request
    ) {
        // Make sure the verification is linked to a real farm.
        verifyFarmExists(farmId);

        // Copy request data into a new FarmVerification entity.
        FarmVerification farmVerification = new FarmVerification();
        farmVerification.setFarmId(farmId);
        applyRequest(farmVerification, request);

        // Save the verification and return API-friendly response data.
        FarmVerification savedFarmVerification = farmVerificationRepository.save(farmVerification);
        return FarmVerificationResponse.from(savedFarmVerification);
    }

    public List<FarmVerificationResponse> getFarmVerifications(UUID farmId) {
        // Make sure the farm exists before listing its verifications.
        verifyFarmExists(farmId);

        // Fetch verifications newest first and convert each one to a response.
        return farmVerificationRepository.findByFarmIdOrderByVerificationDateDesc(farmId)
                .stream()
                .map(FarmVerificationResponse::from)
                .toList();
    }

    public FarmVerificationResponse getLatestFarmVerification(UUID farmId) {
        // Make sure the farm exists before looking for its latest verification.
        verifyFarmExists(farmId);

        // Return the newest verification for this farm.
        FarmVerification farmVerification = farmVerificationRepository
                .findFirstByFarmIdOrderByVerificationDateDesc(farmId)
                .orElseThrow(() -> new RuntimeException("Farm verification not found"));
        return FarmVerificationResponse.from(farmVerification);
    }

    public FarmVerificationResponse getFarmVerification(UUID verificationId) {
        // Load one verification by ID and convert it to a response.
        FarmVerification farmVerification = findFarmVerification(verificationId);
        return FarmVerificationResponse.from(farmVerification);
    }

    public FarmVerificationResponse updateFarmVerification(
            UUID verificationId,
            CreateFarmVerificationRequest request
    ) {
        // Load the existing verification, update its fields, then save it.
        FarmVerification farmVerification = findFarmVerification(verificationId);
        applyRequest(farmVerification, request);

        FarmVerification savedFarmVerification = farmVerificationRepository.save(farmVerification);
        return FarmVerificationResponse.from(savedFarmVerification);
    }

    private FarmVerification findFarmVerification(UUID verificationId) {
        // Reuse one not-found lookup rule for verification reads and updates.
        return farmVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Farm verification not found"));
    }

    private void verifyFarmExists(UUID farmId) {
        // Prevent creating or listing verifications for farms that do not exist.
        if (!farmRepository.existsById(farmId)) {
            throw new RuntimeException("Farm not found");
        }
    }

    private void applyRequest(
            FarmVerification farmVerification,
            CreateFarmVerificationRequest request
    ) {
        // Keep request-to-entity field mapping in one place.
        farmVerification.setVerificationDate(request.verificationDate());
        farmVerification.setVerifiedByUserId(request.verifiedByUserId());
        farmVerification.setVerificationType(request.verificationType());
        farmVerification.setStatus(request.status());
        farmVerification.setChemicalFreeClaim(request.chemicalFreeClaim());
        farmVerification.setAgroecologyVerified(request.agroecologyVerified());
        farmVerification.setChecklistJson(request.checklistJson());
        farmVerification.setObservations(request.observations());
        farmVerification.setNextVerificationDue(request.nextVerificationDue());
    }
}
