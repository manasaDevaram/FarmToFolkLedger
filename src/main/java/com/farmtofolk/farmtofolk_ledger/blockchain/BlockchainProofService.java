package com.farmtofolk.farmtofolk_ledger.blockchain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BlockchainProofService {

  private final BlockchainRecordRepository blockchainRecordRepository;

  public BlockchainProofService(BlockchainRecordRepository blockchainRecordRepository) {
    this.blockchainRecordRepository = blockchainRecordRepository;
  }

  public Optional<BlockchainRecord> createPendingEvidenceProof(UUID evidenceId, String fileHash) {
    if (fileHash == null || fileHash.isBlank()) {
      return Optional.empty();
    }
    // A pending row records proof intent without pretending a blockchain transaction has occurred.
    BlockchainRecord record = new BlockchainRecord();
    record.setEntityType("VERIFICATION_EVIDENCE");
    record.setEntityId(evidenceId);
    record.setRecordHash(fileHash);
    record.setStatus("PENDING");
    return Optional.of(blockchainRecordRepository.save(record));
  }
}
