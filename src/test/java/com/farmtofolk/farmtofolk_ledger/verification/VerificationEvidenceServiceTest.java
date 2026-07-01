package com.farmtofolk.farmtofolk_ledger.verification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.blockchain.BlockchainProofService;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.*;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class VerificationEvidenceServiceTest {

  @Mock VerificationEvidenceRepository evidenceRepository;
  @Mock FarmVerificationRepository verificationRepository;
  @Mock PublicTraceCacheService cacheService;
  @Mock StorageService storageService;
  @Mock FileHashService fileHashService;
  @Mock CurrentUserService currentUserService;
  @Mock BlockchainProofService blockchainProofService;
  @Mock AfterCommitExecutor afterCommitExecutor;
  @Mock DomainEventPublisher domainEventPublisher;
  @Mock PlatformTransactionManager transactionManager;

  @Test
  void uploadAttributesActorHashesBytesAndCreatesPendingProof() {
    UUID verificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    FarmVerification verification = new FarmVerification();
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "photo".getBytes());
    when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(verification));
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(fileHashService.sha256Hex(file)).thenReturn("abc123");
    when(storageService.upload(eq(file), anyString(), any(Set.class)))
        .thenReturn(
            new StoredFileResponse(
                "key", "https://example.com/photo.jpg", "photo.jpg", "image/jpeg", 5L));
    when(currentUserService.getCurrentUserId()).thenReturn(userId);
    when(evidenceRepository.save(any(VerificationEvidence.class)))
        .thenAnswer(
            invocation -> {
              VerificationEvidence evidence = invocation.getArgument(0);
              ReflectionTestUtils.setField(evidence, "id", evidenceId);
              return evidence;
            });
    VerificationEvidenceService service =
        new VerificationEvidenceService(
            evidenceRepository,
            verificationRepository,
            cacheService,
            storageService,
            fileHashService,
            currentUserService,
            blockchainProofService,
            afterCommitExecutor,
            domainEventPublisher,
            transactionManager);

    service.uploadVerificationEvidence(verificationId, file, "Field photo", true);

    ArgumentCaptor<VerificationEvidence> captor =
        ArgumentCaptor.forClass(VerificationEvidence.class);
    verify(evidenceRepository).save(captor.capture());
    assertEquals("abc123", captor.getValue().getFileHash());
    assertEquals(userId, captor.getValue().getUploadedByUserId());
    assertNotNull(captor.getValue().getCapturedAt());
    verify(blockchainProofService).createPendingEvidenceProof(evidenceId, "abc123");
  }

  @Test
  void urlOnlyEvidenceUsesServerActorAndDoesNotCreateBlockchainProof() {
    UUID verificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    FarmVerification verification = new FarmVerification();
    when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(verification));
    when(currentUserService.getCurrentUserId()).thenReturn(userId);
    when(evidenceRepository.save(any(VerificationEvidence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    VerificationEvidenceService service =
        new VerificationEvidenceService(
            evidenceRepository,
            verificationRepository,
            cacheService,
            storageService,
            fileHashService,
            currentUserService,
            blockchainProofService,
            afterCommitExecutor,
            domainEventPublisher,
            transactionManager);

    service.createVerificationEvidence(
        verificationId,
        new CreateVerificationEvidenceRequest(
            "image/jpeg", "https://example.com/evidence.jpg", "URL evidence", true, null));

    ArgumentCaptor<VerificationEvidence> captor =
        ArgumentCaptor.forClass(VerificationEvidence.class);
    verify(evidenceRepository).save(captor.capture());
    assertNull(captor.getValue().getFileHash());
    assertEquals(userId, captor.getValue().getUploadedByUserId());
    assertNotNull(captor.getValue().getCapturedAt());
    verifyNoInteractions(blockchainProofService);
  }

  @Test
  void uploadDeletesS3ObjectWhenDatabaseSaveFails() {
    UUID verificationId = UUID.randomUUID();
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "photo".getBytes());
    when(verificationRepository.findById(verificationId))
        .thenReturn(Optional.of(new FarmVerification()));
    when(currentUserService.getCurrentUserId()).thenReturn(UUID.randomUUID());
    when(fileHashService.sha256Hex(file)).thenReturn("abc123");
    when(storageService.upload(eq(file), anyString(), any(Set.class)))
        .thenReturn(
            new StoredFileResponse(
                "uploaded-key", "https://example.com/photo.jpg", "photo.jpg", "image/jpeg", 5L));
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(evidenceRepository.save(any(VerificationEvidence.class)))
        .thenThrow(new IllegalStateException("database failed"));
    VerificationEvidenceService service =
        new VerificationEvidenceService(
            evidenceRepository,
            verificationRepository,
            cacheService,
            storageService,
            fileHashService,
            currentUserService,
            blockchainProofService,
            afterCommitExecutor,
            domainEventPublisher,
            transactionManager);

    assertThrows(
        IllegalStateException.class,
        () -> service.uploadVerificationEvidence(verificationId, file, null, true));

    verify(storageService).delete("uploaded-key");
    verifyNoInteractions(blockchainProofService);
  }
}
