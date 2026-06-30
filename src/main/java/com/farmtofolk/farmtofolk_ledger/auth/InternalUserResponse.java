package com.farmtofolk.farmtofolk_ledger.auth;

import java.time.LocalDateTime;
import java.util.UUID;

public record InternalUserResponse(
    UUID id,
    String name,
    String email,
    String phone,
    UserRole role,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static InternalUserResponse from(User user) {
    return new InternalUserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getRole(),
        user.getActive(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
