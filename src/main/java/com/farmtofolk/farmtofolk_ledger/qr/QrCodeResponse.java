package com.farmtofolk.farmtofolk_ledger.qr;

import java.time.LocalDateTime;
import java.util.UUID;

public record QrCodeResponse(
        UUID id,
        UUID batchId,
        String publicToken,
        String qrImageUrl,
        String qrType,
        Boolean isActive,
        LocalDateTime generatedAt,
        LocalDateTime expiresAt
) {

    public static QrCodeResponse from(QrCode qrCode) {
        return new QrCodeResponse(
                qrCode.getId(),
                qrCode.getBatchId(),
                qrCode.getPublicToken(),
                qrCode.getQrImageUrl(),
                qrCode.getQrType(),
                qrCode.getIsActive(),
                qrCode.getGeneratedAt(),
                qrCode.getExpiresAt()
        );
    }
}
