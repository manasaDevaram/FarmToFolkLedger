package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class QrImagePersistenceService {

    private final QrCodeRepository qrCodeRepository;

    public QrImagePersistenceService(QrCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    @Transactional
    public void updateImageUrl(UUID qrCodeId, String imageUrl) {
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));
        qrCode.setQrImageUrl(imageUrl);
        qrCodeRepository.save(qrCode);
    }
}
