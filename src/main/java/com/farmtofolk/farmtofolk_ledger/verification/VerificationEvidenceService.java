package com.farmtofolk.farmtofolk_ledger.verification;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VerificationEvidenceService {

    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final FarmVerificationRepository farmVerificationRepository;
    private final PublicTraceCacheService publicTraceCacheService;

    public VerificationEvidenceService(
            VerificationEvidenceRepository verificationEvidenceRepository,
            FarmVerificationRepository farmVerificationRepository,
            PublicTraceCacheService publicTraceCacheService
    ) {
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.farmVerificationRepository = farmVerificationRepository;
        this.publicTraceCacheService = publicTraceCacheService;
    }

    public VerificationEvidenceResponse createVerificationEvidence(
            UUID verificationId,
            CreateVerificationEvidenceRequest request
    ) {
        // Make sure the evidence is linked to a real farm verification.
        FarmVerification farmVerification = findFarmVerification(verificationId);

        // Copy request data into a new VerificationEvidence entity.
        VerificationEvidence verificationEvidence = new VerificationEvidence();
        verificationEvidence.setVerificationId(verificationId);
        applyRequest(verificationEvidence, request);

        // Save the evidence and return API-friendly response data.
        VerificationEvidence savedVerificationEvidence = verificationEvidenceRepository.save(verificationEvidence);
        // Clear QR page stable data because verification evidence changed.
        publicTraceCacheService.evictStableDataForFarm(farmVerification.getFarmId());
        return VerificationEvidenceResponse.from(savedVerificationEvidence);
    }

    public List<VerificationEvidenceResponse> getEvidenceForVerification(UUID verificationId) {
        // Make sure the verification exists before listing its evidence.
        findFarmVerification(verificationId);

        // Fetch evidence oldest first and convert each one to a response.
        return verificationEvidenceRepository.findByVerificationIdOrderByCreatedAtAsc(verificationId)
                .stream()
                .map(VerificationEvidenceResponse::from)
                .toList();
    }

    public void deleteEvidence(UUID evidenceId) {
        // Load evidence first so missing IDs produce the expected message.
        VerificationEvidence verificationEvidence = findVerificationEvidence(evidenceId);
        FarmVerification farmVerification = findFarmVerification(verificationEvidence.getVerificationId());
        verificationEvidenceRepository.delete(verificationEvidence);
        // Clear QR page stable data because verification evidence changed.
        publicTraceCacheService.evictStableDataForFarm(farmVerification.getFarmId());
    }

    private VerificationEvidence findVerificationEvidence(UUID evidenceId) {
        // Reuse one not-found lookup rule for evidence delete operations.
        return verificationEvidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification evidence not found"));
    }

    private FarmVerification findFarmVerification(UUID verificationId) {
        // Reuse one not-found lookup rule for verification evidence operations.
        return farmVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm verification not found"));
    }

    private void applyRequest(
            VerificationEvidence verificationEvidence,
            CreateVerificationEvidenceRequest request
    ) {
        // Keep request-to-entity field mapping in one place.
        verificationEvidence.setFileType(request.fileType());
        verificationEvidence.setFileUrl(request.fileUrl());
        verificationEvidence.setFileHash(request.fileHash());
        verificationEvidence.setCaption(request.caption());
        verificationEvidence.setIsPublic(request.isPublic());
        verificationEvidence.setCapturedAt(request.capturedAt());
        verificationEvidence.setUploadedByUserId(request.uploadedByUserId());
    }
}
