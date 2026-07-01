package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.analytics.ScanAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScanAnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScanAnalyticsEventListener.class);

    private final ScanAnalyticsService scanAnalyticsService;

    public ScanAnalyticsEventListener(ScanAnalyticsService scanAnalyticsService) {
        this.scanAnalyticsService = scanAnalyticsService;
    }

    @Async("domainEventExecutor")
    @EventListener
    public void onScanEventRecorded(ScanEventRecordedEvent event) {
        try {
            scanAnalyticsService.incrementCounters(event);
        } catch (RuntimeException exception) {
            log.warn("Failed to update scan analytics for scan {}", event.scanEventId(), exception);
        }
    }
}
