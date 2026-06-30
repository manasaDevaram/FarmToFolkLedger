package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.NotNull;

public record UpdateInternalUserRoleRequest(@NotNull UserRole role) {}
