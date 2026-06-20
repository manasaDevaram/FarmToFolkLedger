package com.farmtofolk.farmtofolk_ledger.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationEvidenceRepository extends JpaRepository<VerificationEvidence, UUID> {

    List<VerificationEvidence> findByVerificationIdOrderByCreatedAtAsc(UUID verificationId);
}
