ALTER TABLE price_breakdowns
ADD COLUMN operational_cost NUMERIC(19,2);

UPDATE price_breakdowns
SET operational_cost =
  COALESCE(transport_cost, 0)
  + COALESCE(packing_cost, 0)
  + COALESCE(organization_cost, 0)
  + COALESCE(platform_cost, 0);

ALTER TABLE price_breakdowns
DROP COLUMN transport_cost,
DROP COLUMN packing_cost,
DROP COLUMN organization_cost,
DROP COLUMN platform_cost;
