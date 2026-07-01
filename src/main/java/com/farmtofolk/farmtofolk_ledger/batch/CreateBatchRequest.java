package com.farmtofolk.farmtofolk_ledger.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateBatchRequest(
    String batchCode,
    @NotNull UUID farmId,
    @NotNull UUID farmerId,
    @NotBlank String cropName,
    String variety,
    @NotNull @Positive BigDecimal quantityReceived,
    @NotBlank String unit,
    @NotNull LocalDate harvestDate,
    @NotNull LocalDate receivedDate,
    @NotNull @PositiveOrZero BigDecimal farmerPricePerUnit,
    @NotNull PaymentStatus paymentStatus,
    @NotNull @PositiveOrZero BigDecimal consumerPricePerUnit,
    @NotNull @PositiveOrZero BigDecimal operationalCostPerUnit,
    @NotBlank String status) {}
