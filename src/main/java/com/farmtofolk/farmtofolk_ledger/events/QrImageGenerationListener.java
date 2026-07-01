package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.qr.QrImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class QrImageGenerationListener {

    private static final Logger log = LoggerFactory.getLogger(QrImageGenerationListener.class);

    private final QrImageGenerationService qrImageGenerationService;

    public QrImageGenerationListener(QrImageGenerationService qrImageGenerationService) {
        this.qrImageGenerationService = qrImageGenerationService;
    }

    @Async("domainEventExecutor")
    @EventListener
    public void onQrCodeCreated(QrCodeCreatedEvent event) {
        try {
            qrImageGenerationService.generateAndUpload(event);
        } catch (RuntimeException exception) {
            log.warn("Failed to generate QR image for QR code {}", event.qrCodeId(), exception);
        }
    }
}
