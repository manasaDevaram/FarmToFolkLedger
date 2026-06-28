package com.farmtofolk.farmtofolk_ledger.adminpayments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePaymentStatusRequest(
        @NotBlank @Pattern(regexp = "(?i)UNPAID|PAID") String paymentStatus
) {
}
