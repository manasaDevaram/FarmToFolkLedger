package com.farmtofolk.farmtofolk_ledger.farm;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFarmRequest(
        UUID farmerId,
        String farmName,
        String village,
        String district,
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal sizeAcres,
        String farmingType
) {
}
