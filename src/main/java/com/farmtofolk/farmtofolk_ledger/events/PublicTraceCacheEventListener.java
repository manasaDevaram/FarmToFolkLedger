package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PublicTraceCacheEventListener {

    private static final Logger log = LoggerFactory.getLogger(PublicTraceCacheEventListener.class);

    private final PublicTraceCacheService publicTraceCacheService;

    public PublicTraceCacheEventListener(PublicTraceCacheService publicTraceCacheService) {
        this.publicTraceCacheService = publicTraceCacheService;
    }

    @EventListener
    public void onBatchUpdated(BatchUpdatedEvent event) {
        evictBatch(event.batchId(), "batch update");
    }

    @EventListener
    public void onTraceEventCreated(TraceEventCreatedEvent event) {
        evictBatch(event.batchId(), "trace event creation");
    }

    @EventListener
    public void onFarmVerificationChanged(FarmVerificationChangedEvent event) {
        try {
            publicTraceCacheService.evictStableDataForFarm(event.farmId());
        } catch (RuntimeException exception) {
            log.warn("Failed to evict public trace cache after farm verification change {}", event.verificationId(), exception);
        }
    }

    @EventListener
    public void onPublicTraceContentChanged(PublicTraceContentChangedEvent event) {
        try {
            switch (event.scope()) {
                case BATCH -> publicTraceCacheService.evictStableDataForBatch(event.entityId());
                case FARM -> publicTraceCacheService.evictStableDataForFarm(event.entityId());
                case FARMER -> publicTraceCacheService.evictStableDataForFarmer(event.entityId());
            }
        } catch (RuntimeException exception) {
            log.warn("Failed to evict public trace cache for {} {}",
                    event.scope(), event.entityId(), exception);
        }
    }

    private void evictBatch(java.util.UUID batchId, String reason) {
        try {
            publicTraceCacheService.evictStableDataForBatch(batchId);
        } catch (RuntimeException exception) {
            log.warn("Failed to evict public trace cache for batch {} after {}", batchId, reason, exception);
        }
    }
}
