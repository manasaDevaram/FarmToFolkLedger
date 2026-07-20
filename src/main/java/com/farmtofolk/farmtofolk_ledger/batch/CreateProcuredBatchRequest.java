package com.farmtofolk.farmtofolk_ledger.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProcuredBatchRequest(
    @NotNull UUID farmId,
    @NotNull UUID farmerId,
    @NotNull UUID parentBatchId,
    @NotBlank String cropName,
    String variety,
    @NotNull @Positive BigDecimal quantityReceived,
    @NotBlank String unit,
    @NotNull LocalDate harvestDate,
    @NotNull LocalDate receivedDate,
    @NotNull @PositiveOrZero BigDecimal farmerPricePerUnit,
    @NotNull PaymentStatus paymentStatus) {}
