ALTER TABLE batches
  ADD COLUMN quantity_received NUMERIC(19,3),
  ADD COLUMN quantity_sold NUMERIC(19,3) DEFAULT 0,
  ADD COLUMN quantity_wasted NUMERIC(19,3) DEFAULT 0,
  ADD COLUMN quantity_used_in_product NUMERIC(19,3) DEFAULT 0,
  ADD COLUMN quantity_available NUMERIC(19,3),
  ADD COLUMN received_date DATE,
  ADD COLUMN farmer_price_per_unit NUMERIC(19,2),
  ADD COLUMN total_farmer_amount NUMERIC(19,2),
  ADD COLUMN payment_status VARCHAR(32),
  ADD COLUMN consumer_price_per_unit NUMERIC(19,2),
  ADD COLUMN operational_cost_per_unit NUMERIC(19,2);

UPDATE batches b
SET quantity_received = COALESCE(p.quantity_taken, b.quantity, 0),
    quantity_sold = COALESCE((
      SELECT SUM(s.quantity_sold) FROM batch_sale_transactions s WHERE s.batch_id = b.id
    ), 0),
    quantity_wasted = 0,
    quantity_used_in_product = 0,
    received_date = COALESCE(p.procured_at::date, b.harvest_date, CURRENT_DATE),
    farmer_price_per_unit = COALESCE(p.farmer_price_per_unit, pb.farmer_price, 0),
    total_farmer_amount = COALESCE(
      p.farmer_amount_payable,
      COALESCE(p.quantity_taken, b.quantity, 0) * COALESCE(p.farmer_price_per_unit, pb.farmer_price, 0)
    ),
    payment_status = COALESCE(p.payment_status, 'UNPAID'),
    consumer_price_per_unit = COALESCE(pb.consumer_price, 0),
    operational_cost_per_unit = COALESCE(pb.operational_cost, 0)
FROM batch_procurements p
FULL JOIN price_breakdowns pb ON pb.batch_id = p.batch_id
WHERE b.id = COALESCE(p.batch_id, pb.batch_id);

UPDATE batches
SET quantity_received = COALESCE(quantity_received, quantity, 0),
    quantity_sold = COALESCE(quantity_sold, 0),
    quantity_wasted = COALESCE(quantity_wasted, 0),
    quantity_used_in_product = COALESCE(quantity_used_in_product, 0),
    received_date = COALESCE(received_date, harvest_date, CURRENT_DATE),
    farmer_price_per_unit = COALESCE(farmer_price_per_unit, 0),
    payment_status = COALESCE(payment_status, 'UNPAID'),
    consumer_price_per_unit = COALESCE(consumer_price_per_unit, 0),
    operational_cost_per_unit = COALESCE(operational_cost_per_unit, 0);

UPDATE batches
SET quantity_available = quantity_received - quantity_sold - quantity_wasted - quantity_used_in_product,
    total_farmer_amount = quantity_received * farmer_price_per_unit;

ALTER TABLE batches
  ALTER COLUMN quantity_received SET NOT NULL,
  ALTER COLUMN quantity_sold SET NOT NULL,
  ALTER COLUMN quantity_wasted SET NOT NULL,
  ALTER COLUMN quantity_used_in_product SET NOT NULL,
  ALTER COLUMN quantity_available SET NOT NULL,
  ALTER COLUMN received_date SET NOT NULL,
  ALTER COLUMN farmer_price_per_unit SET NOT NULL,
  ALTER COLUMN total_farmer_amount SET NOT NULL,
  ALTER COLUMN payment_status SET NOT NULL,
  ALTER COLUMN consumer_price_per_unit SET NOT NULL,
  ALTER COLUMN operational_cost_per_unit SET NOT NULL,
  DROP COLUMN quantity;

CREATE TABLE batch_usages (
  id UUID PRIMARY KEY,
  batch_id UUID NOT NULL REFERENCES batches(id),
  usage_type VARCHAR(40) NOT NULL,
  quantity NUMERIC(19,3) NOT NULL CHECK (quantity > 0),
  price_per_unit NUMERIC(19,2),
  customer_name VARCHAR(255),
  customer_type VARCHAR(255),
  reason VARCHAR(255),
  notes TEXT,
  recorded_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_batch_usages_batch_recorded ON batch_usages(batch_id, recorded_at);
