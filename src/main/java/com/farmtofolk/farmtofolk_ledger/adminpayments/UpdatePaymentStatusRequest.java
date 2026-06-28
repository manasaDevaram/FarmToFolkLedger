package com.farmtofolk.farmtofolk_ledger.adminpayments;

import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(@NotNull PaymentStatus paymentStatus) {}
