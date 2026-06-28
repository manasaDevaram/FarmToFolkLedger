package com.farmtofolk.farmtofolk_ledger.blockchain;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BlockchainProofService {

    private final BlockchainRecordRepository blockchainRecordRepository;

    public BlockchainProofService(BlockchainRecordRepository blockchainRecordRepository) {
        this.blockchainRecordRepository = blockchainRecordRepository;
    }

    public BlockchainRecord createPendingEvidenceProof(UUID evidenceId, String fileHash) {
        // A pending row records proof intent without pretending a blockchain transaction has occurred.
        BlockchainRecord record = new BlockchainRecord();
        record.setEntityType("VERIFICATION_EVIDENCE");
        record.setEntityId(evidenceId);
        record.setRecordHash(fileHash);
        record.setStatus("PENDING");
        return blockchainRecordRepository.save(record);
    }
}
