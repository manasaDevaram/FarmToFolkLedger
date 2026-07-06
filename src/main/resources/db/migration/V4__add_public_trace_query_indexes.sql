CREATE UNIQUE INDEX IF NOT EXISTS ux_qr_codes_public_token
    ON qr_codes (public_token);

CREATE INDEX IF NOT EXISTS ix_trace_events_batch_created
    ON trace_events (batch_id, created_at);

CREATE INDEX IF NOT EXISTS ix_scan_events_qr_created
    ON scan_events (qr_code_id, created_at);

CREATE INDEX IF NOT EXISTS ix_scan_events_batch_created
    ON scan_events (batch_id, created_at);

CREATE INDEX IF NOT EXISTS ix_farm_verifications_farm_created
    ON farm_verifications (farm_id, created_at);

CREATE INDEX IF NOT EXISTS ix_farm_media_farm
    ON farm_media (farm_id);

CREATE INDEX IF NOT EXISTS ix_verification_evidence_verification
    ON verification_evidence (verification_id);
