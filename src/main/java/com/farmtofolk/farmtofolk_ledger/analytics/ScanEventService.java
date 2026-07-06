package com.farmtofolk.farmtofolk_ledger.analytics;

import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.QrScannedEvent;
import com.farmtofolk.farmtofolk_ledger.events.ScanEventRecordedEvent;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScanEventService {

  private final ScanEventRepository scanEventRepository;
  private final DomainEventPublisher domainEventPublisher;

  public ScanEventService(
      ScanEventRepository scanEventRepository,
      DomainEventPublisher domainEventPublisher) {
    this.scanEventRepository = scanEventRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ScanEventResponse recordScan(QrScannedEvent event) {
    ScanEvent scanEvent = new ScanEvent();
    scanEvent.setQrCodeId(event.qrCodeId());
    scanEvent.setBatchId(event.batchId());
    scanEvent.setPublicToken(event.publicToken());
    scanEvent.setScannedAt(java.time.LocalDateTime.ofInstant(
        event.scannedAt(), java.time.ZoneOffset.UTC));
    scanEvent.setCountry(event.country());
    scanEvent.setState(event.state());
    scanEvent.setCity(event.city());
    scanEvent.setDeviceType(event.deviceType());
    scanEvent.setUserAgent(event.userAgent());
    scanEvent.setIpHash(event.ipHash());

    ScanEvent savedScanEvent = scanEventRepository.save(scanEvent);
    domainEventPublisher.publishAfterCommit(
        new ScanEventRecordedEvent(
            savedScanEvent.getId(), savedScanEvent.getBatchId(), savedScanEvent.getQrCodeId(),
            savedScanEvent.getScannedAt(), savedScanEvent.getCity(), savedScanEvent.getDeviceType()));
    return ScanEventResponse.from(savedScanEvent);
  }

  public long getScanCount(UUID qrCodeId) {
    // Count scans recorded against this QR code.
    return scanEventRepository.countByQrCodeId(qrCodeId);
  }
}
