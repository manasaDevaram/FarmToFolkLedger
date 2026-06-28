package com.farmtofolk.farmtofolk_ledger.auth;

public record LoginResponse(String token, UserResponse user) {
}
