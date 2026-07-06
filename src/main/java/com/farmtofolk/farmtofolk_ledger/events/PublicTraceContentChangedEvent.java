package com.farmtofolk.farmtofolk_ledger.events;

import java.util.UUID;

public record PublicTraceContentChangedEvent(Scope scope, UUID entityId) {

  public enum Scope {
    BATCH,
    FARM,
    FARMER
  }
}
