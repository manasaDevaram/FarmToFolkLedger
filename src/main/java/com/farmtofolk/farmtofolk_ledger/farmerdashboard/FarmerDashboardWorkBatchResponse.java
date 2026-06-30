package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmerDashboardWorkBatchResponse(
    UUID batchId,
    String batchCode,
    String cropName,
    String currentTraceStatus,
    String batchStatus,
    LocalDate harvestDate,
    BigDecimal quantityProduced,
    BigDecimal quantitySold,
    BigDecimal remainingQuantity,
    BigDecimal farmerPrice,
    BigDecimal consumerPrice,
    BigDecimal amountPayable,
    PaymentStatus paymentStatus,
    BigDecimal saleAmount,
    LocalDateTime lastUpdated) {}
