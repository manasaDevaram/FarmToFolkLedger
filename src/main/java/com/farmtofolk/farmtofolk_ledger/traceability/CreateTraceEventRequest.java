package com.farmtofolk.farmtofolk_ledger.traceability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTraceEventRequest(
    @NotBlank
        @Size(max = 80)
        @Pattern(regexp = ".*[A-Za-z0-9].*", message = "must contain a letter or number")
        String eventType,
    @NotNull LocalDateTime eventTime,
    String location,
    String description,
    UUID actorUserId,
    String metadataJson) {}
