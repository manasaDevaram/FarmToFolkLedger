package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateInternalUserRequest(
    String name,
    @Email String email,
    @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits") String phone,
    Boolean active) {}
