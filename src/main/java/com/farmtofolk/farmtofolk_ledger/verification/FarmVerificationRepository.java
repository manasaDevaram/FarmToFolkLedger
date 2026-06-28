package com.farmtofolk.farmtofolk_ledger.verification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmVerificationRepository extends JpaRepository<FarmVerification, UUID> {

  List<FarmVerification> findByFarmIdOrderByVerificationDateDesc(UUID farmId);

  Optional<FarmVerification> findFirstByFarmIdOrderByVerificationDateDesc(UUID farmId);
}
