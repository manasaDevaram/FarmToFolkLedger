package com.farmtofolk.farmtofolk_ledger.procurement;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchProcurementRepository extends JpaRepository<BatchProcurement, UUID> {
  Optional<BatchProcurement> findByBatchId(UUID batchId);

  boolean existsByBatchId(UUID batchId);

  List<BatchProcurement> findByBatchIdIn(Collection<UUID> batchIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select procurement from BatchProcurement procurement where procurement.batchId = :batchId")
  Optional<BatchProcurement> findByBatchIdForUpdate(@Param("batchId") UUID batchId);
}
