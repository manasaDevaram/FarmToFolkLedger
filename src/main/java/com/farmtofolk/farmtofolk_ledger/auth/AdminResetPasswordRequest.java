package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
    @NotBlank @Size(min = 8) String newPassword, @NotBlank String confirmPassword) {}
