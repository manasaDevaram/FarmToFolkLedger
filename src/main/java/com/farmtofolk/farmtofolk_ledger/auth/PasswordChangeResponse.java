package com.farmtofolk.farmtofolk_ledger.auth;

import java.util.UUID;

public record PasswordChangeResponse(
    String message, UUID userId, String email, String phone, UserRole role) {

  public static PasswordChangeResponse from(String message, User user) {
    return new PasswordChangeResponse(
        message, user.getId(), user.getEmail(), user.getPhone(), user.getRole());
  }
}
