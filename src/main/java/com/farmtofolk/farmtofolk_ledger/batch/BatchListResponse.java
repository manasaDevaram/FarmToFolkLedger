package com.farmtofolk.farmtofolk_ledger.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;

public record BatchListResponse(
    UUID id,
    String batchCode,
    UUID farmerId,
    String farmerName,
    UUID farmId,
    String farmName,
    String cropName,
    String variety,
    BigDecimal quantityReceived,
    BigDecimal quantitySold,
    BigDecimal quantityWasted,
    BigDecimal quantityUsedInProduct,
    BigDecimal quantityAvailable,
    String unit,
    LocalDate harvestDate,
    LocalDate receivedDate,
    BigDecimal farmerPricePerUnit,
    BigDecimal totalFarmerAmount,
    PaymentStatus paymentStatus,
    BigDecimal consumerPricePerUnit,
    BigDecimal operationalCostPerUnit,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static BatchListResponse from(Batch batch, String farmerName, String farmName) {
    return new BatchListResponse(
        batch.getId(),
        batch.getBatchCode(),
        batch.getFarmerId(),
        farmerName,
        batch.getFarmId(),
        farmName,
        batch.getCropName(),
        batch.getVariety(),
        batch.getQuantityReceived(),
        batch.getQuantitySold(),
        batch.getQuantityWasted(),
        batch.getQuantityUsedInProduct(),
        batch.getQuantityAvailable(),
        batch.getUnit(),
        batch.getHarvestDate(),
        batch.getReceivedDate(),
        batch.getFarmerPricePerUnit(),
        batch.getTotalFarmerAmount(),
        batch.getPaymentStatus(),
        batch.getConsumerPricePerUnit(),
        batch.getOperationalCostPerUnit(),
        batch.getStatus(),
        batch.getCreatedAt(),
        batch.getUpdatedAt());
  }
}
