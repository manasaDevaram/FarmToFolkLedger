package com.farmtofolk.farmtofolk_ledger.adminoverview;

import java.math.BigDecimal;

public record AdminDashboardResponse(
    Payments payments,
    Verifications verifications,
    Inventory inventory,
    SecondaryCounts secondaryCounts) {
  public record Payments(BigDecimal pendingAmount, long pendingCount) {}
  public record Verifications(long pendingCount, long upcomingCount) {}
  public record Inventory(
      BigDecimal totalAvailableQuantity,
      BigDecimal totalSoldQuantity,
      BigDecimal totalWastedQuantity) {}
  public record SecondaryCounts(long farmers, long farms, long batches) {}
}
