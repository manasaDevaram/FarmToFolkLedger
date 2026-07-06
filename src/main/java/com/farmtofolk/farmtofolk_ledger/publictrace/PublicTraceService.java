package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.QrScannedEvent;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicTraceService {

  private static final Logger log = LoggerFactory.getLogger(PublicTraceService.class);

  private final QrCodeRepository qrCodeRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final ApplicationEventPublisher eventPublisher;

  public PublicTraceService(
      QrCodeRepository qrCodeRepository,
      PublicTraceCacheService publicTraceCacheService,
      ApplicationEventPublisher eventPublisher) {
    this.qrCodeRepository = qrCodeRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.eventPublisher = eventPublisher;
  }

  public PublicTraceResponse getPublicTrace(String publicToken) {
    QrCode qrCode =
        qrCodeRepository
            .findByPublicToken(publicToken)
            .filter(qr -> Boolean.TRUE.equals(qr.getIsActive()))
            .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));
    try {
      eventPublisher.publishEvent(
          new QrScannedEvent(
              publicToken, qrCode.getId(), qrCode.getBatchId(), null, null, null, null, null, null,
              Instant.now()));
      log.debug("Published QR scan event for QR {}", qrCode.getId());
    } catch (RuntimeException exception) {
      log.warn("Failed to publish QR scan event for QR {}", qrCode.getId(), exception);
    }
    return publicTraceCacheService.getFullTrace(publicToken, qrCode);
  }
}
