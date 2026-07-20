package com.farmtofolk.farmtofolk_ledger.qr;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

  Optional<QrCode> findByPublicToken(String publicToken);

  Optional<QrCode> findFirstByBatchIdAndIsActiveTrue(UUID batchId);

  Optional<QrCode> findFirstBySowingBatchIdAndIsActiveTrue(UUID sowingBatchId);

  Optional<QrCode> findByPublicTokenAndIsActiveTrue(String publicToken);
}
