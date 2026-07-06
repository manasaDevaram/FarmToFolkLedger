package com.farmtofolk.farmtofolk_ledger.events;

import java.util.UUID;

public record TraceEventCreatedEvent(UUID batchId, UUID traceEventId) {
}
