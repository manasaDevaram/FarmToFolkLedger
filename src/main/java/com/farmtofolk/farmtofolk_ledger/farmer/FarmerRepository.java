package com.farmtofolk.farmtofolk_ledger.farmer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerRepository extends JpaRepository<Farmer, UUID> {

  boolean existsByFarmerCode(String farmerCode);

  boolean existsByFarmerCodeAndIdNot(String farmerCode, UUID id);

  boolean existsByPhone(String phone);

  boolean existsByPhoneAndIdNot(String phone, UUID id);

  Optional<Farmer> findByUserId(UUID userId);
}
