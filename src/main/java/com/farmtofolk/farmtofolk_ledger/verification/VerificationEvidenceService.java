package com.farmtofolk.farmtofolk_ledger.verification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VerificationEvidenceService {

    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final FarmVerificationRepository farmVerificationRepository;

    public VerificationEvidenceService(
            VerificationEvidenceRepository verificationEvidenceRepository,
            FarmVerificationRepository farmVerificationRepository
    ) {
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.farmVerificationRepository = farmVerificationRepository;
    }

    public VerificationEvidenceResponse createVerificationEvidence(
            UUID verificationId,
            CreateVerificationEvidenceRequest request
    ) {
        // Make sure the evidence is linked to a real farm verification.
        verifyFarmVerificationExists(verificationId);

        // Copy request data into a new VerificationEvidence entity.
        VerificationEvidence verificationEvidence = new VerificationEvidence();
        verificationEvidence.setVerificationId(verificationId);
        applyRequest(verificationEvidence, request);

        // Save the evidence and return API-friendly response data.
        VerificationEvidence savedVerificationEvidence = verificationEvidenceRepository.save(verificationEvidence);
        return VerificationEvidenceResponse.from(savedVerificationEvidence);
    }

    public List<VerificationEvidenceResponse> getEvidenceForVerification(UUID verificationId) {
        // Make sure the verification exists before listing its evidence.
        verifyFarmVerificationExists(verificationId);

        // Fetch evidence oldest first and convert each one to a response.
        return verificationEvidenceRepository.findByVerificationIdOrderByCreatedAtAsc(verificationId)
                .stream()
                .map(VerificationEvidenceResponse::from)
                .toList();
    }

    public void deleteEvidence(UUID evidenceId) {
        // Load evidence first so missing IDs produce the expected message.
        VerificationEvidence verificationEvidence = findVerificationEvidence(evidenceId);
        verificationEvidenceRepository.delete(verificationEvidence);
    }

    private VerificationEvidence findVerificationEvidence(UUID evidenceId) {
        // Reuse one not-found lookup rule for evidence delete operations.
        return verificationEvidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Verification evidence not found"));
    }

    private void verifyFarmVerificationExists(UUID verificationId) {
        // Prevent creating or listing evidence for verifications that do not exist.
        if (!farmVerificationRepository.existsById(verificationId)) {
            throw new RuntimeException("Farm verification not found");
        }
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
