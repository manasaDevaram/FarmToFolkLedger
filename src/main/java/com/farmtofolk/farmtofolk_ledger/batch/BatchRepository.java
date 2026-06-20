package com.farmtofolk.farmtofolk_ledger.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByFarmerId(UUID farmerId);

    List<Batch> findByFarmId(UUID farmId);

    Optional<Batch> findByBatchCode(String batchCode);
}
