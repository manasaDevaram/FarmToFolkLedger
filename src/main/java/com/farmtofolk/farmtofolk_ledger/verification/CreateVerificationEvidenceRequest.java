package com.farmtofolk.farmtofolk_ledger.verification;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateVerificationEvidenceRequest(
        String fileType,
        String fileUrl,
        String fileHash,
        String caption,
        Boolean isPublic,
        LocalDateTime capturedAt,
        UUID uploadedByUserId
) {
}
