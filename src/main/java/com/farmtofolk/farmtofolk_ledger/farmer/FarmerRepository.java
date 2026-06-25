package com.farmtofolk.farmtofolk_ledger.farmer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FarmerRepository extends JpaRepository<Farmer, UUID> {

    boolean existsByFarmerCode(String farmerCode);
}
