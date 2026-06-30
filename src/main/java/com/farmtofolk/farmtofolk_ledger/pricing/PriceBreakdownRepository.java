package com.farmtofolk.farmtofolk_ledger.pricing;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceBreakdownRepository extends JpaRepository<PriceBreakdown, UUID> {

  Optional<PriceBreakdown> findByBatchId(UUID batchId);

  List<PriceBreakdown> findByBatchIdIn(Collection<UUID> batchIds);
}
