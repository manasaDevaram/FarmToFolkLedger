package com.farmtofolk.farmtofolk_ledger.pricing;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceBreakdownRepository extends JpaRepository<PriceBreakdown, UUID> {

  Optional<PriceBreakdown> findByBatchId(UUID batchId);
}
