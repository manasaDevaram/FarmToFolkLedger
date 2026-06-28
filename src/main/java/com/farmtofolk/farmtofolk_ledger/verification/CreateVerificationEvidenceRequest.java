package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateVerificationEvidenceRequest(
    @NotBlank String fileType,
    @NotBlank String fileUrl,
    String caption,
    Boolean isPublic,
    LocalDateTime capturedAt) {}
