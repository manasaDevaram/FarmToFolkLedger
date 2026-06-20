package com.farmtofolk.farmtofolk_ledger.qr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    Optional<QrCode> findFirstByBatchIdAndIsActiveTrue(UUID batchId);

    Optional<QrCode> findByPublicTokenAndIsActiveTrue(String publicToken);
}
