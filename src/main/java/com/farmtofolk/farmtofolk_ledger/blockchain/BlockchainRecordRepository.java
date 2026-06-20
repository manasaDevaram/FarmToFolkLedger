package com.farmtofolk.farmtofolk_ledger.blockchain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, UUID> {
}
