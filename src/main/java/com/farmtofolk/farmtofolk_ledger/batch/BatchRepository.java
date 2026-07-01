package com.farmtofolk.farmtofolk_ledger.batch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

  List<Batch> findByFarmerId(UUID farmerId);

  List<Batch> findByFarmId(UUID farmId);

  Optional<Batch> findByBatchCode(String batchCode);

  boolean existsByBatchCode(String batchCode);

  boolean existsByBatchCodeAndIdNot(String batchCode, UUID id);

  long countByFarmerId(UUID farmerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Batch> findForUpdateById(UUID id);
}
