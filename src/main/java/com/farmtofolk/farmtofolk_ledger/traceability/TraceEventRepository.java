package com.farmtofolk.farmtofolk_ledger.traceability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TraceEventRepository extends JpaRepository<TraceEvent, UUID> {

    List<TraceEvent> findByBatchIdOrderByEventTimeAsc(UUID batchId);
}
