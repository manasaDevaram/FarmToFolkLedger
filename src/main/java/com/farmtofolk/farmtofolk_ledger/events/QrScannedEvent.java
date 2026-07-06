package com.farmtofolk.farmtofolk_ledger.events;

import java.time.Instant;
import java.util.UUID;

public record QrScannedEvent(
    String publicToken,
    UUID qrCodeId,
    UUID batchId,
    String country,
    String state,
    String city,
    String deviceType,
    String userAgent,
    String ipHash,
    Instant scannedAt) {}
