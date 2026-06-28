package com.farmtofolk.farmtofolk_ledger.sales;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchSaleTransactionRepository extends JpaRepository<BatchSaleTransaction, UUID> {
  List<BatchSaleTransaction> findByBatchIdOrderBySoldAtAsc(UUID batchId);
}
