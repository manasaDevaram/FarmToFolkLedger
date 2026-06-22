package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateVerificationEvidenceRequest(
        @NotBlank
        String fileType,
        @NotBlank
        String fileUrl,
        String fileHash,
        String caption,
        Boolean isPublic,
        LocalDateTime capturedAt,
        UUID uploadedByUserId
) {
}
