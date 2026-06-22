package com.farmtofolk.farmtofolk_ledger.farmer;

import jakarta.validation.constraints.NotNull;

public record UpdateFarmerStatusRequest(
        @NotNull
        Boolean active
) {
}
