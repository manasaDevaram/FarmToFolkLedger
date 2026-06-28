package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FarmerDashboardBatchResponse(
    UUID batchId,
    String batchCode,
    String cropName,
    String variety,
    String farmName,
    BigDecimal quantityTaken,
    String unit,
    String batchStatus,
    LocalDate harvestDate,
    LocalDate packedDate,
    LocalDate bestBeforeDate,
    String latestTraceStatus,
    BigDecimal farmerPricePerUnit,
    BigDecimal farmerAmountPayable,
    PaymentStatus paymentStatus,
    BigDecimal totalQuantitySold,
    BigDecimal quantityRemaining,
    BigDecimal totalSaleAmount,
    String currency) {}
