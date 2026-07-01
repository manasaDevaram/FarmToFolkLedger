package com.farmtofolk.farmtofolk_ledger.events;

public record PublicTraceScannedEvent(
        String publicToken,
        String country,
        String state,
        String city,
        String deviceType,
        String userAgent,
        String ipHash
) {
}
