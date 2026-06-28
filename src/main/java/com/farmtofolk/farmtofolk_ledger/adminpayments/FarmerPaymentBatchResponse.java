package com.farmtofolk.farmtofolk_ledger.adminpayments;

import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmerPaymentBatchResponse(
    UUID batchId,
    String batchCode,
    String cropName,
    BigDecimal quantityTaken,
    String unit,
    BigDecimal farmerPricePerUnit,
    BigDecimal farmerAmountPayable,
    PaymentStatus paymentStatus,
    LocalDateTime procuredAt) {}
