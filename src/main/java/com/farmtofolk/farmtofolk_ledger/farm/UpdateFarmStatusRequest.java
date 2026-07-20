package com.farmtofolk.farmtofolk_ledger.farm;

import jakarta.validation.constraints.NotNull;

public record UpdateFarmStatusRequest(@NotNull Boolean active) {}
