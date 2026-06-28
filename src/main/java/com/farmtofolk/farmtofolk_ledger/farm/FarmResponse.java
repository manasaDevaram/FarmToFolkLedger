package com.farmtofolk.farmtofolk_ledger.farm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmResponse(
    UUID id,
    UUID farmerId,
    String farmName,
    String village,
    String district,
    String state,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal sizeAcres,
    String farmingType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static FarmResponse from(Farm farm) {
    return new FarmResponse(
        farm.getId(),
        farm.getFarmerId(),
        farm.getFarmName(),
        farm.getVillage(),
        farm.getDistrict(),
        farm.getState(),
        farm.getLatitude(),
        farm.getLongitude(),
        farm.getSizeAcres(),
        farm.getFarmingType(),
        farm.getCreatedAt(),
        farm.getUpdatedAt());
  }
}
