package com.farmtofolk.farmtofolk_ledger.analytics;

import com.farmtofolk.farmtofolk_ledger.events.ScanEventRecordedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ScanAnalyticsService {

    private static final LocalDate TOTALS_DATE = LocalDate.of(1970, 1, 1);

    private final ScanAnalyticsCounterRepository counterRepository;

    public ScanAnalyticsService(ScanAnalyticsCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Transactional
    public void incrementCounters(ScanEventRecordedEvent event) {
        increment("BATCH", event.batchId().toString(), TOTALS_DATE);
        increment("QR_CODE", event.qrCodeId().toString(), TOTALS_DATE);
        increment("DAILY", "ALL", event.scannedAt().toLocalDate());

        if (event.city() != null && !event.city().isBlank()) {
            increment("CITY", event.city().trim().toLowerCase(), TOTALS_DATE);
        }
        if (event.deviceType() != null && !event.deviceType().isBlank()) {
            increment("DEVICE", event.deviceType().trim().toLowerCase(), TOTALS_DATE);
        }
    }

    private void increment(String metricType, String dimensionValue, LocalDate scanDate) {
        counterRepository.increment(UUID.randomUUID(), metricType, dimensionValue, scanDate);
    }
}
