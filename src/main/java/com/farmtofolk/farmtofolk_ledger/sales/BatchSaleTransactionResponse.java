package com.farmtofolk.farmtofolk_ledger.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchSaleTransactionResponse(
    UUID id,
    UUID batchId,
    BigDecimal quantitySold,
    BigDecimal salePricePerUnit,
    BigDecimal saleAmount,
    String currency,
    LocalDateTime soldAt,
    LocalDateTime createdAt) {
  public static BatchSaleTransactionResponse from(BatchSaleTransaction transaction) {
    return new BatchSaleTransactionResponse(
        transaction.getId(),
        transaction.getBatchId(),
        transaction.getQuantitySold(),
        transaction.getSalePricePerUnit(),
        transaction.getSaleAmount(),
        transaction.getCurrency(),
        transaction.getSoldAt(),
        transaction.getCreatedAt());
  }
}
