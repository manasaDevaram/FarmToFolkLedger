package com.farmtofolk.farmtofolk_ledger.qr;

import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;

public record QrCodeResponse(
    UUID id,
    UUID batchId,
    String publicToken,
    String qrImageUrl,
    String qrType,
    Boolean isActive,
    LocalDateTime generatedAt) {

  public static QrCodeResponse from(QrCode qrCode) {
    return new QrCodeResponse(
        qrCode.getId(),
        qrCode.getBatchId(),
        qrCode.getPublicToken(),
        qrCode.getQrImageUrl(),
        qrCode.getQrType(),
        qrCode.getIsActive(),
        qrCode.getGeneratedAt());
  }

  public static QrCodeResponse from(QrCode qrCode, StorageService storageService) {
    QrCodeResponse response = from(qrCode);
    return new QrCodeResponse(
        response.id(),
        response.batchId(),
        response.publicToken(),
        storageService.generatePresignedUrl(response.qrImageUrl()),
        response.qrType(),
        response.isActive(),
        response.generatedAt());
  }
}
