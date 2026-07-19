-- Wipes all application data from the public schema.
-- Run against RDS, then restart the API with BOOTSTRAP_ADMIN_ENABLED=true
-- to create exactly one admin user (see scripts/README.md).

BEGIN;

-- Legacy tables (safe if already dropped by V5)
DROP TABLE IF EXISTS batch_sale_transactions CASCADE;
DROP TABLE IF EXISTS batch_procurements CASCADE;
DROP TABLE IF EXISTS price_breakdowns CASCADE;
DROP TABLE IF EXISTS blockchain_records CASCADE;

TRUNCATE TABLE
  scan_analytics_counters,
  scan_events,
  batch_usages,
  trace_events,
  qr_codes,
  verification_evidence,
  farm_verifications,
  farm_media,
  batches,
  farms,
  farmers,
  users
RESTART IDENTITY CASCADE;

COMMIT;
