package com.farmtofolk.farmtofolk_ledger.traceability;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTraceEventRequest(
        String eventType,
        LocalDateTime eventTime,
        String location,
        String description,
        UUID actorUserId,
        String metadataJson
) {
}
