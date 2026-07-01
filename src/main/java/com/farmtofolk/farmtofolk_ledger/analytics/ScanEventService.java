package com.farmtofolk.farmtofolk_ledger.analytics;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.ScanEventRecordedEvent;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScanEventService {

  private final ScanEventRepository scanEventRepository;
  private final QrCodeRepository qrCodeRepository;
  private final DomainEventPublisher domainEventPublisher;

  public ScanEventService(
      ScanEventRepository scanEventRepository,
      QrCodeRepository qrCodeRepository,
      DomainEventPublisher domainEventPublisher) {
    this.scanEventRepository = scanEventRepository;
    this.qrCodeRepository = qrCodeRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ScanEventResponse recordScan(
      String publicToken,
      String country,
      String state,
      String city,
      String deviceType,
      String userAgent,
      String ipHash) {
    // Resolve the public token to an active QR code before recording a scan.
    QrCode qrCode =
        qrCodeRepository
            .findByPublicTokenAndIsActiveTrue(publicToken)
            .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));

    // Store only analytics metadata and the hashed IP value.
    ScanEvent scanEvent = new ScanEvent();
    scanEvent.setQrCodeId(qrCode.getId());
    scanEvent.setBatchId(qrCode.getBatchId());
    scanEvent.setPublicToken(publicToken);
    scanEvent.setScannedAt(LocalDateTime.now());
    scanEvent.setCountry(country);
    scanEvent.setState(state);
    scanEvent.setCity(city);
    scanEvent.setDeviceType(deviceType);
    scanEvent.setUserAgent(userAgent);
    scanEvent.setIpHash(ipHash);

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
