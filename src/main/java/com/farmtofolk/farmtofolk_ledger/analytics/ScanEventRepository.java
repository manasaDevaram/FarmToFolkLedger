package com.farmtofolk.farmtofolk_ledger.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScanEventRepository extends JpaRepository<ScanEvent, UUID> {
}
