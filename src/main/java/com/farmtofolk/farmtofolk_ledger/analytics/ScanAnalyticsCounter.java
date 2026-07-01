package com.farmtofolk.farmtofolk_ledger.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "scan_analytics_counters",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_scan_analytics_metric_dimension_date",
                columnNames = {"metric_type", "dimension_value", "scan_date"}
        )
)
public class ScanAnalyticsCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(name = "dimension_value", nullable = false)
    private String dimensionValue;

    @Column(name = "scan_date", nullable = false)
    private LocalDate scanDate;

    @Column(name = "scan_count", nullable = false)
    private long scanCount;

    public UUID getId() {
        return id;
    }

    public String getMetricType() {
        return metricType;
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public LocalDate getScanDate() {
        return scanDate;
    }

    public long getScanCount() {
        return scanCount;
    }
}
