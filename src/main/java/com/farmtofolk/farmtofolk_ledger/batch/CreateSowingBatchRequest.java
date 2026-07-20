package com.farmtofolk.farmtofolk_ledger.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSowingBatchRequest(
    @NotNull UUID farmId,
    @NotNull UUID farmerId,
    @NotBlank String cropName,
    String variety,
    @NotNull @Positive BigDecimal acresSown,
    @NotNull LocalDate sowingDate) {}
