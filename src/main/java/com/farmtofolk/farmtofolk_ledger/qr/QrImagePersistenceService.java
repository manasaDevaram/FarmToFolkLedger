package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.BatchUpdatedEvent;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class QrImagePersistenceService {

    private final QrCodeRepository qrCodeRepository;
    private final DomainEventPublisher domainEventPublisher;

    public QrImagePersistenceService(
            QrCodeRepository qrCodeRepository,
            DomainEventPublisher domainEventPublisher) {
        this.qrCodeRepository = qrCodeRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    public void updateImageUrl(UUID qrCodeId, String imageUrl) {
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));
        qrCode.setQrImageUrl(imageUrl);
        qrCodeRepository.save(qrCode);
        domainEventPublisher.publishAfterCommit(new BatchUpdatedEvent(qrCode.getBatchId()));
    }
}
