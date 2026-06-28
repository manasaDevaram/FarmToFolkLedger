package com.farmtofolk.farmtofolk_ledger.farm;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmRepository extends JpaRepository<Farm, UUID> {

  List<Farm> findByFarmerId(UUID farmerId);

  long countByFarmerId(UUID farmerId);
}
