ALTER TABLE batches
    ADD COLUMN batch_type VARCHAR(20) NOT NULL DEFAULT 'PROCURED';

ALTER TABLE batches
    ADD COLUMN parent_batch_id UUID;

ALTER TABLE batches
    ADD COLUMN sowing_date DATE;

ALTER TABLE batches
    ADD COLUMN acres_sown NUMERIC(19, 3);

ALTER TABLE batches
    ALTER COLUMN quantity_received DROP NOT NULL;

ALTER TABLE batches
    ALTER COLUMN received_date DROP NOT NULL;

ALTER TABLE batches
    ALTER COLUMN farmer_price_per_unit DROP NOT NULL;

UPDATE batches
SET batch_type = 'PROCURED'
WHERE batch_type IS NULL;

ALTER TABLE qr_codes
    ADD COLUMN sowing_batch_id UUID;

UPDATE qr_codes
SET sowing_batch_id = batch_id
WHERE sowing_batch_id IS NULL;

CREATE INDEX IF NOT EXISTS ix_batches_parent_batch_id ON batches (parent_batch_id);
CREATE INDEX IF NOT EXISTS ix_batches_batch_type ON batches (batch_type);
CREATE INDEX IF NOT EXISTS ix_qr_codes_sowing_batch_id ON qr_codes (sowing_batch_id);
