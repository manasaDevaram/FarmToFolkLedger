package com.farmtofolk.farmtofolk_ledger.auth;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String phone,
    UserRole role,
    String gender,
    String address,
    Boolean active) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getRole(),
        user.getGender(),
        user.getAddress(),
        user.getActive());
  }
}
