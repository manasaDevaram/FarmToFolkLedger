package com.farmtofolk.farmtofolk_ledger.verification;

import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;

public record VerificationEvidenceResponse(
    UUID id,
    UUID verificationId,
    String fileType,
    String fileUrl,
    String fileKey,
    String fileHash,
    String contentType,
    Long sizeBytes,
    String caption,
    Boolean isPublic,
    LocalDateTime capturedAt,
    UUID uploadedByUserId,
    LocalDateTime createdAt) {

  public static VerificationEvidenceResponse from(VerificationEvidence verificationEvidence) {
    return new VerificationEvidenceResponse(
        verificationEvidence.getId(),
        verificationEvidence.getVerificationId(),
        verificationEvidence.getFileType(),
        verificationEvidence.getFileUrl(),
        verificationEvidence.getFileKey(),
        verificationEvidence.getFileHash(),
        verificationEvidence.getContentType(),
        verificationEvidence.getSizeBytes(),
        verificationEvidence.getCaption(),
        verificationEvidence.getIsPublic(),
        verificationEvidence.getCapturedAt(),
        verificationEvidence.getUploadedByUserId(),
        verificationEvidence.getCreatedAt());
  }

  public static VerificationEvidenceResponse from(
      VerificationEvidence verificationEvidence, StorageService storageService) {
    return from(verificationEvidence).withPresignedUrl(storageService);
  }

  public VerificationEvidenceResponse withPresignedUrl(StorageService storageService) {
    String storedValue = fileKey != null ? fileKey : fileUrl;
    return new VerificationEvidenceResponse(
        id, verificationId, fileType, storageService.generatePresignedUrl(storedValue), fileKey,
        fileHash, contentType, sizeBytes, caption, isPublic, capturedAt, uploadedByUserId, createdAt);
  }
}
