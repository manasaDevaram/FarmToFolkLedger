package com.farmtofolk.farmtofolk_ledger.publictrace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidence;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicVerificationResolverTest {

  @Mock private FarmVerificationRepository farmVerificationRepository;

  @Mock private VerificationEvidenceRepository verificationEvidenceRepository;

  @InjectMocks private PublicVerificationResolver publicVerificationResolver;

  @Test
  void resolveForFarmReturnsLastVerifiedWithPublicEvidenceOnly() {
    UUID farmId = UUID.randomUUID();
    UUID verificationId = UUID.randomUUID();

    FarmVerification verification = new FarmVerification();
    ReflectionTestUtils.setField(verification, "id", verificationId);
    verification.setFarmId(farmId);
    verification.setStatus("VERIFIED");
    verification.setVerificationDate(LocalDate.of(2026, 5, 17));

    VerificationEvidence publicEvidence = new VerificationEvidence();
    publicEvidence.setVerificationId(verificationId);
    publicEvidence.setIsPublic(true);

    VerificationEvidence privateEvidence = new VerificationEvidence();
    privateEvidence.setVerificationId(verificationId);
    privateEvidence.setIsPublic(false);

    when(farmVerificationRepository.findFirstByFarmIdAndStatusIgnoreCaseInOrderByVerificationDateDesc(
            farmId, List.of("VERIFIED", "APPROVED")))
        .thenReturn(Optional.of(verification));
    when(verificationEvidenceRepository.findByVerificationIdOrderByCreatedAtAsc(verificationId))
        .thenReturn(List.of(publicEvidence, privateEvidence));

    PublicVerificationResolver.PublicVerificationSnapshot snapshot =
        publicVerificationResolver.resolveForFarm(farmId);

    assertEquals(LocalDate.of(2026, 5, 17), snapshot.verification().verificationDate());
    assertEquals(1, snapshot.evidence().size());
  }

  @Test
  void resolveForFarmReturnsVerifiedRecordEvenWithoutPublicEvidence() {
    UUID farmId = UUID.randomUUID();
    UUID verificationId = UUID.randomUUID();

    FarmVerification verification = new FarmVerification();
    ReflectionTestUtils.setField(verification, "id", verificationId);
    verification.setFarmId(farmId);
    verification.setStatus("VERIFIED");
    verification.setVerificationDate(LocalDate.of(2026, 5, 17));

    when(farmVerificationRepository.findFirstByFarmIdAndStatusIgnoreCaseInOrderByVerificationDateDesc(
            farmId, List.of("VERIFIED", "APPROVED")))
        .thenReturn(Optional.of(verification));
    when(verificationEvidenceRepository.findByVerificationIdOrderByCreatedAtAsc(verificationId))
        .thenReturn(List.of());

    PublicVerificationResolver.PublicVerificationSnapshot snapshot =
        publicVerificationResolver.resolveForFarm(farmId);

    assertEquals(LocalDate.of(2026, 5, 17), snapshot.verification().verificationDate());
    assertEquals(0, snapshot.evidence().size());
  }

  @Test
  void resolveForFarmFiltersOutPrivateEvidence() {
    UUID farmId = UUID.randomUUID();
    UUID verificationId = UUID.randomUUID();

    FarmVerification verification = new FarmVerification();
    ReflectionTestUtils.setField(verification, "id", verificationId);
    verification.setStatus("VERIFIED");
    verification.setVerificationDate(LocalDate.of(2026, 5, 17));

    VerificationEvidence privateEvidence = new VerificationEvidence();
    privateEvidence.setVerificationId(verificationId);
    privateEvidence.setIsPublic(false);

    when(farmVerificationRepository.findFirstByFarmIdAndStatusIgnoreCaseInOrderByVerificationDateDesc(
            farmId, List.of("VERIFIED", "APPROVED")))
        .thenReturn(Optional.of(verification));
    when(verificationEvidenceRepository.findByVerificationIdOrderByCreatedAtAsc(verificationId))
        .thenReturn(List.of(privateEvidence));

    PublicVerificationResolver.PublicVerificationSnapshot snapshot =
        publicVerificationResolver.resolveForFarm(farmId);

    assertEquals(LocalDate.of(2026, 5, 17), snapshot.verification().verificationDate());
    assertEquals(0, snapshot.evidence().size());
  }
}
