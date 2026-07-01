package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.analytics.ScanAnalyticsService;
import com.farmtofolk.farmtofolk_ledger.analytics.ScanEventService;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventListenerTest {

    @Test
    void scanRecordingFailureDoesNotEscapeListener() {
        ScanEventService scanEventService = mock(ScanEventService.class);
        ScanEventListener listener = new ScanEventListener(scanEventService);
        PublicTraceScannedEvent event = new PublicTraceScannedEvent(
                "token", null, null, null, null, null, null
        );
        doThrow(new IllegalStateException("database unavailable"))
                .when(scanEventService)
                .recordScan("token", null, null, null, null, null, null);

        assertDoesNotThrow(() -> listener.onPublicTraceScanned(event));
    }

    @Test
    void cacheEvictionListenerRespondsToDomainEvents() {
        PublicTraceCacheService cacheService = mock(PublicTraceCacheService.class);
        PublicTraceCacheEventListener listener = new PublicTraceCacheEventListener(cacheService);
        UUID batchId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();

        listener.onBatchUpdated(new BatchUpdatedEvent(batchId));
        listener.onTraceEventCreated(new TraceEventCreatedEvent(batchId, UUID.randomUUID()));
        listener.onFarmVerificationChanged(new FarmVerificationChangedEvent(farmId, UUID.randomUUID()));

        verify(cacheService, org.mockito.Mockito.times(2)).evictStableDataForBatch(batchId);
        verify(cacheService).evictStableDataForFarm(farmId);
    }

    @Test
    void analyticsFailureDoesNotEscapeListener() {
        ScanAnalyticsService analyticsService = mock(ScanAnalyticsService.class);
        ScanAnalyticsEventListener listener = new ScanAnalyticsEventListener(analyticsService);
        ScanEventRecordedEvent event = new ScanEventRecordedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null
        );
        doThrow(new IllegalStateException("analytics unavailable"))
                .when(analyticsService)
                .incrementCounters(event);

        assertDoesNotThrow(() -> listener.onScanEventRecorded(event));
    }
}
