CREATE TABLE IF NOT EXISTS scan_analytics_counters (
    id UUID PRIMARY KEY,
    metric_type VARCHAR(40) NOT NULL,
    dimension_value VARCHAR(255) NOT NULL,
    scan_date DATE NOT NULL,
    scan_count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_scan_analytics_metric_dimension_date
        UNIQUE (metric_type, dimension_value, scan_date)
);
