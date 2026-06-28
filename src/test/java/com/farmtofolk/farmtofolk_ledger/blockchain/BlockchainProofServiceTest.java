package com.farmtofolk.farmtofolk_ledger.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlockchainProofServiceTest {

  @Mock BlockchainRecordRepository blockchainRecordRepository;

  @Test
  void hashCreatesPendingBlockchainRecord() {
    UUID evidenceId = UUID.randomUUID();
    when(blockchainRecordRepository.save(any(BlockchainRecord.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    new BlockchainProofService(blockchainRecordRepository)
        .createPendingEvidenceProof(evidenceId, "sha256-hash");

    ArgumentCaptor<BlockchainRecord> captor = ArgumentCaptor.forClass(BlockchainRecord.class);
    verify(blockchainRecordRepository).save(captor.capture());
    assertEquals(evidenceId, captor.getValue().getEntityId());
    assertEquals("sha256-hash", captor.getValue().getRecordHash());
    assertEquals("PENDING", captor.getValue().getStatus());
  }

  @Test
  void nullHashDoesNotCreateBlockchainRecord() {
    assertTrue(
        new BlockchainProofService(blockchainRecordRepository)
            .createPendingEvidenceProof(UUID.randomUUID(), null)
            .isEmpty());

    verify(blockchainRecordRepository, never()).save(any());
  }
}
