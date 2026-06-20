package com.farmtofolk.farmtofolk_ledger.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FarmVerificationRepository extends JpaRepository<FarmVerification, UUID> {
}
