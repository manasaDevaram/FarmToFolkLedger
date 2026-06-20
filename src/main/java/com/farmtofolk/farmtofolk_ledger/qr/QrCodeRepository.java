package com.farmtofolk.farmtofolk_ledger.qr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
}
