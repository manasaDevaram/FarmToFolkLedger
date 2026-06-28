package com.farmtofolk.farmtofolk_ledger.analytics;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScanEventResponse(
    UUID id,
    UUID qrCodeId,
    String publicToken,
    LocalDateTime scannedAt,
    String country,
    String state,
    String city,
    String deviceType,
    String userAgent,
    String ipHash,
    LocalDateTime createdAt) {

  public static ScanEventResponse from(ScanEvent scanEvent) {
    return new ScanEventResponse(
        scanEvent.getId(),
        scanEvent.getQrCodeId(),
        scanEvent.getPublicToken(),
        scanEvent.getScannedAt(),
        scanEvent.getCountry(),
        scanEvent.getState(),
        scanEvent.getCity(),
        scanEvent.getDeviceType(),
        scanEvent.getUserAgent(),
        scanEvent.getIpHash(),
        scanEvent.getCreatedAt());
  }
}
