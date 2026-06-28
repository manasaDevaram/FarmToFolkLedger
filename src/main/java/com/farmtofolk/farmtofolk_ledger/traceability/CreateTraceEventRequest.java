package com.farmtofolk.farmtofolk_ledger.traceability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTraceEventRequest(
    @NotBlank String eventType,
    @NotNull LocalDateTime eventTime,
    String location,
    String description,
    UUID actorUserId,
    String metadataJson) {}
