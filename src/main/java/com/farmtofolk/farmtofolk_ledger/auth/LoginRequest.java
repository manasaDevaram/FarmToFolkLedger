package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String emailOrPhone, @NotBlank String password) {}
