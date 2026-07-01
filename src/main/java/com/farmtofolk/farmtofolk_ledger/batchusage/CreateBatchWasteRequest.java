package com.farmtofolk.farmtofolk_ledger.batchusage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateBatchWasteRequest(
    @NotNull @Positive BigDecimal quantity,
    String reason,
    String notes,
    LocalDateTime recordedAt) {}
