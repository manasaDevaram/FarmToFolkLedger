package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.analytics.ScanEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class QrScanAnalyticsListener {

  private static final Logger log = LoggerFactory.getLogger(QrScanAnalyticsListener.class);

  private final ScanEventService scanEventService;

  public QrScanAnalyticsListener(ScanEventService scanEventService) {
    this.scanEventService = scanEventService;
  }

  @Async("scanEventExecutor")
  @EventListener
  public void onQrScanned(QrScannedEvent event) {
    log.debug("Received QR scan event for QR {}", event.qrCodeId());
    try {
      scanEventService.recordScan(event);
      log.debug("Recorded QR scan event for QR {}", event.qrCodeId());
    } catch (RuntimeException exception) {
      log.warn("Failed to record QR scan event for QR {}", event.qrCodeId(), exception);
    }
  }
}
