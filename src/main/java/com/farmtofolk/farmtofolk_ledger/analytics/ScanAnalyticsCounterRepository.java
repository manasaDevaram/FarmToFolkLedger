package com.farmtofolk.farmtofolk_ledger.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface ScanAnalyticsCounterRepository extends JpaRepository<ScanAnalyticsCounter, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO scan_analytics_counters
                (id, metric_type, dimension_value, scan_date, scan_count)
            VALUES
                (:id, :metricType, :dimensionValue, :scanDate, 1)
            ON CONFLICT (metric_type, dimension_value, scan_date)
            DO UPDATE SET scan_count = scan_analytics_counters.scan_count + 1
            """, nativeQuery = true)
    void increment(
            UUID id,
            String metricType,
            String dimensionValue,
            LocalDate scanDate
    );
}
