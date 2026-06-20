package com.farmtofolk.farmtofolk_ledger.traceability;

import java.time.LocalDateTime;
import java.util.UUID;

public record TraceEventResponse(
        UUID id,
        UUID batchId,
        String eventType,
        LocalDateTime eventTime,
        String location,
        String description,
        UUID actorUserId,
        String metadataJson,
        LocalDateTime createdAt
) {

    public static TraceEventResponse from(TraceEvent traceEvent) {
        return new TraceEventResponse(
                traceEvent.getId(),
                traceEvent.getBatchId(),
                traceEvent.getEventType(),
                traceEvent.getEventTime(),
                traceEvent.getLocation(),
                traceEvent.getDescription(),
                traceEvent.getActorUserId(),
                traceEvent.getMetadataJson(),
                traceEvent.getCreatedAt()
        );
    }
}
