package com.farmtofolk.farmtofolk_ledger.events;

import java.util.UUID;

public record QrCodeCreatedEvent(UUID qrCodeId, UUID batchId, String publicToken) {
}
