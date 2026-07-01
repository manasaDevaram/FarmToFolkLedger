package com.farmtofolk.farmtofolk_ledger.batchusage;

public enum BatchUsageType {
  SOLD_ONLINE,
  SOLD_OFFLINE,
  CAFE,
  EXPERIENCE_CENTRE,
  USED_IN_PRODUCT,
  WASTED;

  public boolean requiresPrice() {
    return this == SOLD_ONLINE
        || this == SOLD_OFFLINE
        || this == CAFE
        || this == EXPERIENCE_CENTRE;
  }

  public boolean isSale() {
    return requiresPrice();
  }
}
