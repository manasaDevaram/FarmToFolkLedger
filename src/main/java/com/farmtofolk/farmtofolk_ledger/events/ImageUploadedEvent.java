package com.farmtofolk.farmtofolk_ledger.events;

import java.util.UUID;

public record ImageUploadedEvent(String entityType, UUID entityId, String originalUrl) {
}
