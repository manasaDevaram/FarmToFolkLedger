package com.farmtofolk.farmtofolk_ledger.blockchain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, UUID> {}
