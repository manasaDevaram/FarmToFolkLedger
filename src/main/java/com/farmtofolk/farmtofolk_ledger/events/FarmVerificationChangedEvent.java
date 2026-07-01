package com.farmtofolk.farmtofolk_ledger.events;

import java.util.UUID;

public record FarmVerificationChangedEvent(UUID farmId, UUID verificationId) {
}
