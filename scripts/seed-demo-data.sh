#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

api_get() {
  curl -fsS "$BASE_URL$1"
}

api_get_optional() {
  local path="$1"
  local body_file
  local status

  body_file="$(mktemp)"
  status="$(curl -sS -o "$body_file" -w "%{http_code}" "$BASE_URL$path")"

  if [[ "$status" == "200" ]]; then
    cat "$body_file"
    rm -f "$body_file"
    return 0
  fi

  if [[ "$status" == "404" ]]; then
    rm -f "$body_file"
    return 1
  fi

  cat "$body_file" >&2
  rm -f "$body_file"
  echo "Request failed: GET $path returned HTTP $status" >&2
  exit 1
}

api_post() {
  local path="$1"
  local payload="$2"

  curl -fsS \
    -H "Content-Type: application/json" \
    -X POST \
    -d "$payload" \
    "$BASE_URL$path"
}

api_put() {
  local path="$1"
  local payload="$2"

  curl -fsS \
    -H "Content-Type: application/json" \
    -X PUT \
    -d "$payload" \
    "$BASE_URL$path"
}

create_or_get_farmer() {
  local farmer_code="$1"
  local name="$2"
  local phone="$3"
  local village="$4"
  local district="$5"
  local state="$6"
  local bio="$7"
  local joined_date="$8"
  local existing_id
  local payload

  existing_id="$(api_get "/api/farmers" | jq -r --arg farmerCode "$farmer_code" '.[] | select(.farmerCode == $farmerCode) | .id' | head -n 1)"

  if [[ -n "$existing_id" ]]; then
    echo "$existing_id"
    return 0
  fi

  payload="$(jq -n \
    --arg farmerCode "$farmer_code" \
    --arg name "$name" \
    --arg phone "$phone" \
    --arg village "$village" \
    --arg district "$district" \
    --arg state "$state" \
    --arg bio "$bio" \
    --arg joinedDate "$joined_date" \
    '{
      farmerCode: $farmerCode,
      name: $name,
      phone: $phone,
      village: $village,
      district: $district,
      state: $state,
      bio: $bio,
      profilePhotoUrl: null,
      introVideoUrl: null,
      joinedDate: $joinedDate
    }')"

  api_post "/api/farmers" "$payload" | jq -r '.id'
}

create_or_get_farm() {
  local farmer_id="$1"
  local farm_name="$2"
  local village="$3"
  local district="$4"
  local state="$5"
  local latitude="$6"
  local longitude="$7"
  local size_acres="$8"
  local farming_type="$9"
  local existing_id
  local payload

  existing_id="$(api_get "/api/farmers/$farmer_id/farms" | jq -r --arg farmName "$farm_name" '.[] | select(.farmName == $farmName) | .id' | head -n 1)"

  if [[ -n "$existing_id" ]]; then
    echo "$existing_id"
    return 0
  fi

  payload="$(jq -n \
    --arg farmerId "$farmer_id" \
    --arg farmName "$farm_name" \
    --arg village "$village" \
    --arg district "$district" \
    --arg state "$state" \
    --arg farmingType "$farming_type" \
    --argjson latitude "$latitude" \
    --argjson longitude "$longitude" \
    --argjson sizeAcres "$size_acres" \
    '{
      farmerId: $farmerId,
      farmName: $farmName,
      village: $village,
      district: $district,
      state: $state,
      latitude: $latitude,
      longitude: $longitude,
      sizeAcres: $sizeAcres,
      farmingType: $farmingType
    }')"

  api_post "/api/farms" "$payload" | jq -r '.id'
}

create_or_get_batch() {
  local farmer_id="$1"
  local farm_id="$2"
  local batch_code="$3"
  local crop_name="$4"
  local variety="$5"
  local quantity="$6"
  local unit="$7"
  local harvest_date="$8"
  local existing_id
  local payload

  existing_id="$(api_get "/api/farms/$farm_id/batches" | jq -r --arg batchCode "$batch_code" '.[] | select(.batchCode == $batchCode) | .id' | head -n 1)"

  if [[ -n "$existing_id" ]]; then
    echo "$existing_id"
    return 0
  fi

  payload="$(jq -n \
    --arg batchCode "$batch_code" \
    --arg farmId "$farm_id" \
    --arg farmerId "$farmer_id" \
    --arg cropName "$crop_name" \
    --arg variety "$variety" \
    --arg unit "$unit" \
    --arg harvestDate "$harvest_date" \
    --arg status "READY_FOR_SALE" \
    --argjson quantity "$quantity" \
    '{
      batchCode: $batchCode,
      farmId: $farmId,
      farmerId: $farmerId,
      cropName: $cropName,
      variety: $variety,
      quantity: $quantity,
      unit: $unit,
      harvestDate: $harvestDate,
      status: $status
    }')"

  api_post "/api/batches" "$payload" | jq -r '.id'
}

create_or_get_verification() {
  local farm_id="$1"
  local verification_type="$2"
  local verification_date="$3"
  local observations="$4"
  local existing_id
  local payload

  existing_id="$(api_get "/api/farms/$farm_id/verifications" | jq -r --arg verificationType "$verification_type" --arg verificationDate "$verification_date" '.[] | select(.verificationType == $verificationType and .verificationDate == $verificationDate) | .id' | head -n 1)"

  if [[ -n "$existing_id" ]]; then
    echo "$existing_id"
    return 0
  fi

  payload="$(jq -n \
    --arg verificationDate "$verification_date" \
    --arg verificationType "$verification_type" \
    --arg status "VERIFIED" \
    --arg observations "$observations" \
    --arg nextVerificationDue "2026-12-01" \
    '{
      verificationDate: $verificationDate,
      verifiedByUserId: null,
      verificationType: $verificationType,
      status: $status,
      chemicalFreeClaim: true,
      agroecologyVerified: true,
      checklistJson: "{\"soilHealth\":true,\"chemicalFree\":true,\"waterConservation\":true}",
      observations: $observations,
      nextVerificationDue: $nextVerificationDue
    }')"

  api_post "/api/farms/$farm_id/verifications" "$payload" | jq -r '.id'
}

upsert_price_breakdown() {
  local batch_id="$1"
  local consumer_price="$2"
  local farmer_price="$3"
  local logistics="$4"
  local platform_fee="$5"
  local retail_margin="$6"
  local payload

  payload="$(jq -n \
    --arg currency "INR" \
    --arg priceUnit "per kg" \
    --argjson consumerPrice "$consumer_price" \
    --argjson farmerPrice "$farmer_price" \
    --argjson logistics "$logistics" \
    --argjson retailMargin "$retail_margin" \
    --argjson platformFee "$platform_fee" \
    '{
      consumerPrice: $consumerPrice,
      farmerPrice: $farmerPrice,
      operationalCost: ($logistics + $retailMargin + $platformFee),
      currency: $currency,
      priceUnit: $priceUnit
    }')"

  if api_get_optional "/api/batches/$batch_id/price-breakdown" >/dev/null; then
    api_put "/api/batches/$batch_id/price-breakdown" "$payload" | jq -r '.id'
  else
    api_post "/api/batches/$batch_id/price-breakdown" "$payload" | jq -r '.id'
  fi
}

create_trace_event_if_missing() {
  local batch_id="$1"
  local event_type="$2"
  local event_time="$3"
  local location="$4"
  local description="$5"
  local existing_id
  local payload

  existing_id="$(api_get "/api/batches/$batch_id/trace-events" | jq -r --arg eventType "$event_type" '.[] | select(.eventType == $eventType) | .id' | head -n 1)"

  if [[ -n "$existing_id" ]]; then
    echo "$existing_id"
    return 0
  fi

  payload="$(jq -n \
    --arg eventType "$event_type" \
    --arg eventTime "$event_time" \
    --arg location "$location" \
    --arg description "$description" \
    '{
      eventType: $eventType,
      eventTime: $eventTime,
      location: $location,
      description: $description,
      actorUserId: null,
      metadataJson: null
    }')"

  api_post "/api/batches/$batch_id/trace-events" "$payload" | jq -r '.id'
}

create_or_get_qr() {
  local batch_id="$1"

  api_post "/api/batches/$batch_id/qr-code" "" | jq -r '{id, publicToken}'
}

seed_trace_events() {
  local batch_id="$1"
  local crop_key="$2"
  local harvest_date="$3"
  local market_location="$4"

  case "$crop_key" in
    tomato)
      create_trace_event_if_missing "$batch_id" "HARVESTED" "${harvest_date}T06:30:00" "Nanjangud Natural Farm" "Tomatoes harvested early morning and sorted at the farm." >/dev/null
      create_trace_event_if_missing "$batch_id" "PACKED" "${harvest_date}T10:00:00" "Nanjangud Natural Farm" "Tomatoes packed in reusable ventilated crates to protect freshness." >/dev/null
      create_trace_event_if_missing "$batch_id" "RECEIVED_AT_MARKET" "${harvest_date}T15:30:00" "$market_location" "Batch received at Mysuru collection center after farm-level quality check." >/dev/null
      create_trace_event_if_missing "$batch_id" "SOLD" "${harvest_date}T18:15:00" "$market_location" "Sold through FarmToFolk traceable produce network." >/dev/null
      ;;
    onion)
      create_trace_event_if_missing "$batch_id" "HARVESTED" "${harvest_date}T07:00:00" "Hunsur Agroecology Farm" "Red onions harvested after field curing and graded by bulb size." >/dev/null
      create_trace_event_if_missing "$batch_id" "PACKED" "${harvest_date}T11:30:00" "Hunsur Agroecology Farm" "Onions packed in breathable mesh sacks for safe transport." >/dev/null
      create_trace_event_if_missing "$batch_id" "RECEIVED_AT_MARKET" "${harvest_date}T16:00:00" "$market_location" "Batch received at Mysuru collection center and checked for moisture damage." >/dev/null
      create_trace_event_if_missing "$batch_id" "SOLD" "${harvest_date}T19:00:00" "$market_location" "Sold through FarmToFolk traceable produce network." >/dev/null
      ;;
    rice)
      create_trace_event_if_missing "$batch_id" "HARVESTED" "${harvest_date}T08:00:00" "Mandya Mixed Crop Farm" "Sona Masoori paddy harvested from a chemical-free field block." >/dev/null
      create_trace_event_if_missing "$batch_id" "PACKED" "${harvest_date}T14:00:00" "Mandya Mixed Crop Farm" "Rice cleaned, dried, and packed in labeled food-grade sacks." >/dev/null
      create_trace_event_if_missing "$batch_id" "RECEIVED_AT_MARKET" "${harvest_date}T17:30:00" "$market_location" "Rice batch received at Mandya aggregation point for traceable dispatch." >/dev/null
      create_trace_event_if_missing "$batch_id" "SOLD" "${harvest_date}T20:00:00" "$market_location" "Sold through FarmToFolk traceable staples network." >/dev/null
      ;;
    toor-dal)
      create_trace_event_if_missing "$batch_id" "HARVESTED" "${harvest_date}T07:45:00" "Mandya Mixed Crop Farm" "Toor harvested at maturity and dried naturally before processing." >/dev/null
      create_trace_event_if_missing "$batch_id" "PACKED" "${harvest_date}T13:15:00" "Mandya Mixed Crop Farm" "Toor dal cleaned and packed separately from other crop batches." >/dev/null
      create_trace_event_if_missing "$batch_id" "RECEIVED_AT_MARKET" "${harvest_date}T17:00:00" "$market_location" "Toor dal batch received at Mandya aggregation point for quality check." >/dev/null
      create_trace_event_if_missing "$batch_id" "SOLD" "${harvest_date}T19:30:00" "$market_location" "Sold through FarmToFolk traceable staples network." >/dev/null
      ;;
  esac
}

print_kv() {
  printf "%-26s %s\n" "$1:" "$2"
}

require_command curl
require_command jq

echo "Seeding FarmToFolk demo data through REST APIs"
echo "BASE_URL=$BASE_URL"
echo

farmer1_id="$(create_or_get_farmer "DEMO-FARMER-001" "Ramesh Gowda" "+919880000001" "Hullahalli" "Mysuru" "Karnataka" "Natural farmer growing seasonal vegetables near Nanjangud." "2026-06-01")"
farmer2_id="$(create_or_get_farmer "DEMO-FARMER-002" "Lakshmi Devi" "+919880000002" "Bilikere" "Mysuru" "Karnataka" "Agroecology farmer focused on onion and mixed vegetable cultivation." "2026-06-01")"
farmer3_id="$(create_or_get_farmer "DEMO-FARMER-003" "Hanumanthappa" "+919880000003" "Maddur" "Mandya" "Karnataka" "Mixed crop farmer cultivating rice, pulses, and seasonal staples." "2026-06-01")"

farm1_id="$(create_or_get_farm "$farmer1_id" "Nanjangud Natural Farm" "Hullahalli" "Mysuru" "Karnataka" 12.1173 76.6831 3.5 "Natural Farming")"
farm2_id="$(create_or_get_farm "$farmer2_id" "Hunsur Agroecology Farm" "Bilikere" "Mysuru" "Karnataka" 12.3036 76.2916 4.2 "Agroecology")"
farm3_id="$(create_or_get_farm "$farmer3_id" "Mandya Mixed Crop Farm" "Maddur" "Mandya" "Karnataka" 12.5839 77.0451 6.8 "Mixed Crop Natural Farming")"

tomato_batch_id="$(create_or_get_batch "$farmer1_id" "$farm1_id" "DEMO-BATCH-TOMATO-20260624" "Nati Tomato" "Local Nati" 250 "kg" "2026-06-24")"
onion_batch_id="$(create_or_get_batch "$farmer2_id" "$farm2_id" "DEMO-BATCH-ONION-20260622" "Red Onion" "Local Red" 400 "kg" "2026-06-22")"
rice_batch_id="$(create_or_get_batch "$farmer3_id" "$farm3_id" "DEMO-BATCH-RICE-20260615" "Sona Masoori Rice" "Sona Masoori" 800 "kg" "2026-06-15")"
toor_batch_id="$(create_or_get_batch "$farmer3_id" "$farm3_id" "DEMO-BATCH-TOOR-20260618" "Toor Dal" "Local Toor" 300 "kg" "2026-06-18")"

verification1_id="$(create_or_get_verification "$farm1_id" "Verified Agroecology Practices" "2026-06-01" "Farm follows natural soil nutrition practices and avoids synthetic chemical inputs.")"
verification2_id="$(create_or_get_verification "$farm2_id" "Verified Agroecology Practices" "2026-06-01" "Farm uses crop rotation, composting, and low-input pest management practices.")"
verification3_id="$(create_or_get_verification "$farm3_id" "Verified Agroecology Practices" "2026-06-01" "Mixed crop farm verified for soil health, biodiversity, and chemical-free practices.")"

upsert_price_breakdown "$tomato_batch_id" 80 52 10 8 10 >/dev/null
upsert_price_breakdown "$onion_batch_id" 45 28 6 4 7 >/dev/null
upsert_price_breakdown "$rice_batch_id" 70 48 8 5 9 >/dev/null
upsert_price_breakdown "$toor_batch_id" 140 95 15 10 20 >/dev/null

seed_trace_events "$tomato_batch_id" "tomato" "2026-06-24" "Mysuru Collection Center"
seed_trace_events "$onion_batch_id" "onion" "2026-06-22" "Mysuru Collection Center"
seed_trace_events "$rice_batch_id" "rice" "2026-06-15" "Mandya Aggregation Point"
seed_trace_events "$toor_batch_id" "toor-dal" "2026-06-18" "Mandya Aggregation Point"

tomato_qr="$(create_or_get_qr "$tomato_batch_id")"
onion_qr="$(create_or_get_qr "$onion_batch_id")"
rice_qr="$(create_or_get_qr "$rice_batch_id")"
toor_qr="$(create_or_get_qr "$toor_batch_id")"

tomato_qr_id="$(jq -r '.id' <<< "$tomato_qr")"
onion_qr_id="$(jq -r '.id' <<< "$onion_qr")"
rice_qr_id="$(jq -r '.id' <<< "$rice_qr")"
toor_qr_id="$(jq -r '.id' <<< "$toor_qr")"

tomato_public_token="$(jq -r '.publicToken' <<< "$tomato_qr")"
onion_public_token="$(jq -r '.publicToken' <<< "$onion_qr")"
rice_public_token="$(jq -r '.publicToken' <<< "$rice_qr")"
toor_public_token="$(jq -r '.publicToken' <<< "$toor_qr")"

echo "Farmer IDs"
print_kv "Ramesh Gowda" "$farmer1_id"
print_kv "Lakshmi Devi" "$farmer2_id"
print_kv "Hanumanthappa" "$farmer3_id"
echo

echo "Farm IDs"
print_kv "Nanjangud Natural Farm" "$farm1_id"
print_kv "Hunsur Agroecology Farm" "$farm2_id"
print_kv "Mandya Mixed Crop Farm" "$farm3_id"
echo

echo "Batch IDs"
print_kv "Nati Tomato" "$tomato_batch_id"
print_kv "Red Onion" "$onion_batch_id"
print_kv "Sona Masoori Rice" "$rice_batch_id"
print_kv "Toor Dal" "$toor_batch_id"
echo

echo "Verification IDs"
print_kv "Nanjangud Natural Farm" "$verification1_id"
print_kv "Hunsur Agroecology Farm" "$verification2_id"
print_kv "Mandya Mixed Crop Farm" "$verification3_id"
echo

echo "QR IDs and Public Tokens"
print_kv "Nati Tomato QR" "$tomato_qr_id"
print_kv "Nati Tomato Token" "$tomato_public_token"
print_kv "Red Onion QR" "$onion_qr_id"
print_kv "Red Onion Token" "$onion_public_token"
print_kv "Sona Masoori QR" "$rice_qr_id"
print_kv "Sona Masoori Token" "$rice_public_token"
print_kv "Toor Dal QR" "$toor_qr_id"
print_kv "Toor Dal Token" "$toor_public_token"
echo

echo "Public Trace URLs"
echo "$BASE_URL/api/public/trace/$tomato_public_token"
echo "$BASE_URL/api/public/trace/$onion_public_token"
echo "$BASE_URL/api/public/trace/$rice_public_token"
echo "$BASE_URL/api/public/trace/$toor_public_token"
