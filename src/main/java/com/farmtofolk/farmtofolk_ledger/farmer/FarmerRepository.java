package com.farmtofolk.farmtofolk_ledger.farmer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerRepository extends JpaRepository<Farmer, UUID> {

  boolean existsByFarmerCode(String farmerCode);

  Optional<Farmer> findByUserId(UUID userId);
}
