package com.farmtofolk.farmtofolk_ledger.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInternalUserRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits")
        String phone,
    @NotNull UserRole role,
    Boolean active,
    @JsonAlias("password") @NotBlank @Size(min = 8) String initialPassword) {}
