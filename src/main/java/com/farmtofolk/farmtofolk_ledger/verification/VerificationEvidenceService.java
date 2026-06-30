package com.farmtofolk.farmtofolk_ledger.verification;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.blockchain.BlockchainProofService;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.FileHashService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VerificationEvidenceService {

  private static final Set<String> VERIFICATION_EVIDENCE_CONTENT_TYPES =
      Set.of(
          "image/jpeg",
          "image/png",
          "image/webp",
          "video/mp4",
          "video/quicktime",
          "application/pdf");

  private final VerificationEvidenceRepository verificationEvidenceRepository;
  private final FarmVerificationRepository farmVerificationRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final StorageService storageService;
  private final FileHashService fileHashService;
  private final CurrentUserService currentUserService;
  private final BlockchainProofService blockchainProofService;
  private final AfterCommitExecutor afterCommitExecutor;
  private final TransactionTemplate transactionTemplate;

  public VerificationEvidenceService(
      VerificationEvidenceRepository verificationEvidenceRepository,
      FarmVerificationRepository farmVerificationRepository,
      PublicTraceCacheService publicTraceCacheService,
      StorageService storageService,
      FileHashService fileHashService,
      CurrentUserService currentUserService,
      BlockchainProofService blockchainProofService,
      AfterCommitExecutor afterCommitExecutor,
      PlatformTransactionManager transactionManager) {
    this.verificationEvidenceRepository = verificationEvidenceRepository;
    this.farmVerificationRepository = farmVerificationRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.storageService = storageService;
    this.fileHashService = fileHashService;
    this.currentUserService = currentUserService;
    this.blockchainProofService = blockchainProofService;
    this.afterCommitExecutor = afterCommitExecutor;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @Transactional
  public VerificationEvidenceResponse createVerificationEvidence(
      UUID verificationId, CreateVerificationEvidenceRequest request) {
    // Make sure the evidence is linked to a real farm verification.
    FarmVerification farmVerification = findFarmVerification(verificationId);

    // Copy request data into a new VerificationEvidence entity.
    VerificationEvidence verificationEvidence = new VerificationEvidence();
    verificationEvidence.setVerificationId(verificationId);
    applyRequest(verificationEvidence, request);

    // Save the evidence and return API-friendly response data.
    VerificationEvidence savedVerificationEvidence =
        verificationEvidenceRepository.save(verificationEvidence);
    // Clear QR page stable data because verification evidence changed.
    afterCommitExecutor.run(
        () -> publicTraceCacheService.evictStableDataForFarm(farmVerification.getFarmId()));
    return VerificationEvidenceResponse.from(savedVerificationEvidence);
  }

  public VerificationEvidenceResponse uploadVerificationEvidence(
      UUID verificationId, MultipartFile file, String caption, Boolean isPublic) {
    // Make sure the uploaded evidence is linked to a real farm verification.
    FarmVerification farmVerification = findFarmVerification(verificationId);
    UUID uploadedByUserId = currentUserService.getCurrentUserId();

    // Store the file in S3 and keep only metadata in PostgreSQL.
    String fileHash = fileHashService.sha256Hex(file);
    StoredFileResponse storedFile =
        storageService.upload(
            file, "verification-evidence/" + verificationId, VERIFICATION_EVIDENCE_CONTENT_TYPES);

    VerificationEvidenceResponse response;
    try {
      response =
          transactionTemplate.execute(
              status -> {
                VerificationEvidence evidence = new VerificationEvidence();
                evidence.setVerificationId(verificationId);
                evidence.setFileType(storedFile.contentType());
                evidence.setFileUrl(storedFile.fileUrl());
                evidence.setFileKey(storedFile.fileKey());
                evidence.setFileHash(fileHash);
                evidence.setContentType(storedFile.contentType());
                evidence.setSizeBytes(storedFile.sizeBytes());
                evidence.setCaption(caption);
                evidence.setIsPublic(isPublic);
                evidence.setCapturedAt(LocalDateTime.now());
                evidence.setUploadedByUserId(uploadedByUserId);

                VerificationEvidence saved = verificationEvidenceRepository.save(evidence);
                blockchainProofService.createPendingEvidenceProof(saved.getId(), fileHash);
                return VerificationEvidenceResponse.from(saved);
              });
    } catch (RuntimeException exception) {
      deleteUploadedFileSafely(storedFile.fileKey());
      throw exception;
    }

    // The evidence and pending proof have committed before consumers see the new cache value.
    publicTraceCacheService.evictStableDataForFarm(farmVerification.getFarmId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<VerificationEvidenceResponse> getEvidenceForVerification(UUID verificationId) {
    // Make sure the verification exists before listing its evidence.
    findFarmVerification(verificationId);

    // Fetch evidence oldest first and convert each one to a response.
    return verificationEvidenceRepository
        .findByVerificationIdOrderByCreatedAtAsc(verificationId)
        .stream()
        .map(VerificationEvidenceResponse::from)
        .toList();
  }

  @Transactional
  public void deleteEvidence(UUID evidenceId) {
    // Load evidence first so missing IDs produce the expected message.
    VerificationEvidence verificationEvidence = findVerificationEvidence(evidenceId);
    FarmVerification farmVerification =
        findFarmVerification(verificationEvidence.getVerificationId());
    verificationEvidenceRepository.delete(verificationEvidence);
    // Clear QR page stable data because verification evidence changed.
    afterCommitExecutor.run(
        () -> publicTraceCacheService.evictStableDataForFarm(farmVerification.getFarmId()));
  }

  private VerificationEvidence findVerificationEvidence(UUID evidenceId) {
    // Reuse one not-found lookup rule for evidence delete operations.
    return verificationEvidenceRepository
        .findById(evidenceId)
        .orElseThrow(() -> new ResourceNotFoundException("Verification evidence not found"));
  }

  private FarmVerification findFarmVerification(UUID verificationId) {
    // Reuse one not-found lookup rule for verification evidence operations.
    return farmVerificationRepository
        .findById(verificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Farm verification not found"));
  }

  private void applyRequest(
      VerificationEvidence verificationEvidence, CreateVerificationEvidenceRequest request) {
    // Keep request-to-entity field mapping in one place.
    verificationEvidence.setFileType(request.fileType());
    verificationEvidence.setFileUrl(request.fileUrl());
    // URL-only evidence has no server-observed bytes, so it cannot claim a trusted file hash.
    verificationEvidence.setFileHash(null);
    verificationEvidence.setCaption(request.caption());
    verificationEvidence.setIsPublic(request.isPublic());
    verificationEvidence.setCapturedAt(
        request.capturedAt() == null ? LocalDateTime.now() : request.capturedAt());
    verificationEvidence.setUploadedByUserId(currentUserService.getCurrentUserId());
  }

  private void deleteUploadedFileSafely(String fileKey) {
    try {
      storageService.delete(fileKey);
    } catch (RuntimeException ignored) {
      // Keep the original transaction failure as the API error.
    }
  }
}
