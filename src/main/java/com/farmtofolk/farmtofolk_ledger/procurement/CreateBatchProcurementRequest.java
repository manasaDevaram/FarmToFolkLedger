package com.farmtofolk.farmtofolk_ledger.procurement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateBatchProcurementRequest(
        @NotNull @DecimalMin("0.0") BigDecimal quantityTaken,
        @NotNull @DecimalMin("0.0") BigDecimal farmerPricePerUnit,
        @NotBlank @Pattern(regexp = "(?i)UNPAID|PAID") String paymentStatus,
        String currency,
        LocalDateTime procuredAt
) {
}
