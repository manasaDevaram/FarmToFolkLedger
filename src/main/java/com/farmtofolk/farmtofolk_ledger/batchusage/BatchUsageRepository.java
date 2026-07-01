package com.farmtofolk.farmtofolk_ledger.batchusage;

import java.util.List;
import java.util.UUID;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchUsageRepository extends JpaRepository<BatchUsage, UUID> {
  List<BatchUsage> findByBatchIdOrderByRecordedAtAsc(UUID batchId);
  List<BatchUsage> findByBatchIdInOrderByRecordedAtAsc(Collection<UUID> batchIds);
}
