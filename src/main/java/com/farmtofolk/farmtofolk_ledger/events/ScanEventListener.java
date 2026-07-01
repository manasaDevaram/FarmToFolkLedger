package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.analytics.ScanEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScanEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScanEventListener.class);

    private final ScanEventService scanEventService;

    public ScanEventListener(ScanEventService scanEventService) {
        this.scanEventService = scanEventService;
    }

    @Async("domainEventExecutor")
    @EventListener
    public void onPublicTraceScanned(PublicTraceScannedEvent event) {
        try {
            scanEventService.recordScan(
                    event.publicToken(),
                    event.country(),
                    event.state(),
                    event.city(),
                    event.deviceType(),
                    event.userAgent(),
                    event.ipHash()
            );
        } catch (RuntimeException exception) {
            log.warn("Failed to record public trace scan for token {}", event.publicToken(), exception);
        }
    }
}
