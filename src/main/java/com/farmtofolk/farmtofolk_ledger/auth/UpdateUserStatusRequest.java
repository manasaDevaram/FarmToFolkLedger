package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull Boolean active) {
}
