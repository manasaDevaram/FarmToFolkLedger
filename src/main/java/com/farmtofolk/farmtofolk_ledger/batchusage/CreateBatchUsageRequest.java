package com.farmtofolk.farmtofolk_ledger.batchusage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateBatchUsageRequest(
    @NotNull BatchUsageType usageType,
    @NotNull @Positive BigDecimal quantity,
    @PositiveOrZero BigDecimal pricePerUnit,
    String customerName,
    String customerType,
    String reason,
    String notes,
    LocalDateTime recordedAt) {}
