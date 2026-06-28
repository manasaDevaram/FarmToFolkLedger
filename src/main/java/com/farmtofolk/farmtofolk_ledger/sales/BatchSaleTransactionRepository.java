package com.farmtofolk.farmtofolk_ledger.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BatchSaleTransactionRepository extends JpaRepository<BatchSaleTransaction, UUID> {
    List<BatchSaleTransaction> findByBatchIdOrderBySoldAtAsc(UUID batchId);
}
