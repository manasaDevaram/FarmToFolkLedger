-- Consolidate price breakdown onto batches, then drop legacy tables/columns.

ALTER TABLE batches
  ADD COLUMN IF NOT EXISTS wastage_cost NUMERIC(19,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS packaging_cost NUMERIC(19,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS currency VARCHAR(8) DEFAULT 'INR',
  ADD COLUMN IF NOT EXISTS price_unit VARCHAR(32);

UPDATE batches b
SET consumer_price_per_unit = COALESCE(pb.consumer_price, b.consumer_price_per_unit),
    farmer_price_per_unit = COALESCE(pb.farmer_price, b.farmer_price_per_unit),
    operational_cost_per_unit = COALESCE(pb.operational_cost, b.operational_cost_per_unit),
    wastage_cost = COALESCE(pb.wastage_cost, 0),
    packaging_cost = COALESCE(pb.packaging_cost, 0),
    currency = COALESCE(pb.currency, 'INR'),
    price_unit = COALESCE(pb.price_unit, b.unit)
FROM price_breakdowns pb
WHERE pb.batch_id = b.id;

UPDATE batches
SET wastage_cost = COALESCE(wastage_cost, 0),
    packaging_cost = COALESCE(packaging_cost, 0),
    currency = COALESCE(currency, 'INR'),
    price_unit = COALESCE(price_unit, unit)
WHERE wastage_cost IS NULL
   OR packaging_cost IS NULL
   OR currency IS NULL
   OR price_unit IS NULL;

ALTER TABLE batches
  ALTER COLUMN wastage_cost SET NOT NULL,
  ALTER COLUMN packaging_cost SET NOT NULL,
  ALTER COLUMN currency SET NOT NULL;

ALTER TABLE qr_codes DROP COLUMN IF EXISTS expires_at;

DROP TABLE IF EXISTS batch_sale_transactions;
DROP TABLE IF EXISTS batch_procurements;
DROP TABLE IF EXISTS price_breakdowns;
DROP TABLE IF EXISTS blockchain_records;
