package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateUserRequest(
    @NotBlank String name,
    @Email String email,
    @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits") String phone,
    @NotBlank @Size(min = 8) String password,
    @NotNull UserRole role,
    String gender,
    String address,
    UUID farmerId) {}
