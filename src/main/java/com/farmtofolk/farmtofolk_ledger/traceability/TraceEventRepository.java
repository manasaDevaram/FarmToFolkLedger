package com.farmtofolk.farmtofolk_ledger.traceability;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraceEventRepository extends JpaRepository<TraceEvent, UUID> {

  List<TraceEvent> findByBatchIdOrderByEventTimeAsc(UUID batchId);
}
