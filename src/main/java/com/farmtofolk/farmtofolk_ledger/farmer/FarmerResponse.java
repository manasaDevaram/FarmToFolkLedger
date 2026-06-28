package com.farmtofolk.farmtofolk_ledger.farmer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmerResponse(
    UUID id,
    String farmerCode,
    String name,
    String phone,
    String village,
    String district,
    String state,
    String bio,
    String profilePhotoUrl,
    String introVideoUrl,
    LocalDate joinedDate,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static FarmerResponse from(Farmer farmer) {
    return new FarmerResponse(
        farmer.getId(),
        farmer.getFarmerCode(),
        farmer.getName(),
        farmer.getPhone(),
        farmer.getVillage(),
        farmer.getDistrict(),
        farmer.getState(),
        farmer.getBio(),
        farmer.getProfilePhotoUrl(),
        farmer.getIntroVideoUrl(),
        farmer.getJoinedDate(),
        farmer.getActive(),
        farmer.getCreatedAt(),
        farmer.getUpdatedAt());
  }
}
