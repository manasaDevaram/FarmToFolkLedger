package com.farmtofolk.farmtofolk_ledger.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanEventRecordedEvent(
        UUID scanEventId,
        UUID batchId,
        UUID qrCodeId,
        LocalDateTime scannedAt,
        String city,
        String deviceType
) {
}
