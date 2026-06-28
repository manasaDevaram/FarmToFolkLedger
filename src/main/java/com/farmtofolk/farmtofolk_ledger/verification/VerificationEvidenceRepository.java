package com.farmtofolk.farmtofolk_ledger.verification;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationEvidenceRepository extends JpaRepository<VerificationEvidence, UUID> {

  List<VerificationEvidence> findByVerificationIdOrderByCreatedAtAsc(UUID verificationId);
}
