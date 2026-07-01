package com.farmtofolk.farmtofolk_ledger.farmer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;

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
        farmer.getProfilePhotoKey() != null ? farmer.getProfilePhotoKey() : farmer.getProfilePhotoUrl(),
        farmer.getIntroVideoKey() != null ? farmer.getIntroVideoKey() : farmer.getIntroVideoUrl(),
        farmer.getJoinedDate(),
        farmer.getActive(),
        farmer.getCreatedAt(),
        farmer.getUpdatedAt());
  }

  public static FarmerResponse from(Farmer farmer, StorageService storageService) {
    return from(farmer).withPresignedUrls(storageService);
  }

  public FarmerResponse withPresignedUrls(StorageService storageService) {
    return new FarmerResponse(
        id, farmerCode, name, phone, village, district, state, bio,
        storageService.generatePresignedUrl(profilePhotoUrl),
        storageService.generatePresignedUrl(introVideoUrl), joinedDate, active, createdAt, updatedAt);
  }
}
