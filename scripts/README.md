# Demo Seed Data

This folder contains a demo seed script for FarmToFolk Ledger.

The script creates realistic demo data through the existing REST APIs only. It does not insert directly into PostgreSQL and does not require SQL scripts.

## Requirements

- Running backend
- `curl`
- `jq`

Default backend URL:

```bash
http://localhost:8080
```

Override it when needed:

```bash
BASE_URL=http://your-host:8080 ./scripts/seed-demo-data.sh
```

## Run

```bash
./scripts/seed-demo-data.sh
```

## Expected Data

The script creates or reuses:

- 3 farmers
- 3 farms
- 4 batches
- 16 trace events
- 4 price breakdowns
- 3 farm verifications
- 4 QR codes

Repeated runs should not create duplicate farmers, farms, batches, verifications, price breakdowns, trace events, or QR codes where the existing APIs expose enough lookup data.

## Expected Output

The script prints:

- Farmer IDs
- Farm IDs
- Batch IDs
- Verification IDs
- QR IDs
- Public tokens
- Public trace URLs

Example:

```text
Public Trace URLs
http://localhost:8080/api/public/trace/{publicToken}
```

## Assumptions

- `GET /api/farmers` returns all farmers and includes `farmerCode`.
- `GET /api/farmers/{farmerId}/farms` returns farms for duplicate checks.
- `GET /api/farms/{farmId}/batches` returns batches for duplicate checks.
- `GET /api/batches/{batchId}/trace-events` returns existing trace events.
- `POST /api/batches/{batchId}/qr-code` returns the active QR code if one already exists.
- Demo logistics, platform fee, and retail margin are aggregated into `operationalCost`.
- The requested `PACKED` trace event requires the backend trace API to allow `PACKED`.
