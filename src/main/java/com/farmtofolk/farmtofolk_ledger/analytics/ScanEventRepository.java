package com.farmtofolk.farmtofolk_ledger.analytics;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanEventRepository extends JpaRepository<ScanEvent, UUID> {

  long countByQrCodeId(UUID qrCodeId);

  List<ScanEvent> findByQrCodeIdOrderByScannedAtDesc(UUID qrCodeId);
}
