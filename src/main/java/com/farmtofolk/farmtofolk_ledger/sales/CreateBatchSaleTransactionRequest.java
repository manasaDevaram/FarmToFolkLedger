package com.farmtofolk.farmtofolk_ledger.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateBatchSaleTransactionRequest(
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantitySold,
    @NotNull @DecimalMin("0.0") BigDecimal salePricePerUnit,
    String currency,
    LocalDateTime soldAt) {}
