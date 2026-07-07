package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidence;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PublicVerificationResolver {

  private static final List<String> PUBLIC_STATUSES = List.of("VERIFIED", "APPROVED");

  private final FarmVerificationRepository farmVerificationRepository;
  private final VerificationEvidenceRepository verificationEvidenceRepository;

  public PublicVerificationResolver(
      FarmVerificationRepository farmVerificationRepository,
      VerificationEvidenceRepository verificationEvidenceRepository) {
    this.farmVerificationRepository = farmVerificationRepository;
    this.verificationEvidenceRepository = verificationEvidenceRepository;
  }

  public PublicVerificationSnapshot resolveForFarm(UUID farmId) {
    FarmVerification verification =
        farmVerificationRepository
            .findFirstByFarmIdAndStatusIgnoreCaseInOrderByVerificationDateDesc(
                farmId, PUBLIC_STATUSES)
            .orElse(null);
    if (verification == null) {
      return PublicVerificationSnapshot.empty();
    }

    List<VerificationEvidenceResponse> publicEvidence =
        verificationEvidenceRepository
            .findByVerificationIdOrderByCreatedAtAsc(verification.getId())
            .stream()
            .filter(this::isPublicEvidence)
            .map(VerificationEvidenceResponse::from)
            .toList();

    if (publicEvidence.isEmpty()) {
      return PublicVerificationSnapshot.empty();
    }

    return new PublicVerificationSnapshot(
        PublicTraceVerificationResponse.from(verification), publicEvidence);
  }

  private boolean isPublicEvidence(VerificationEvidence evidence) {
    return Boolean.TRUE.equals(evidence.getIsPublic());
  }

  public record PublicVerificationSnapshot(
      PublicTraceVerificationResponse lastVerified, List<VerificationEvidenceResponse> evidence) {

    static PublicVerificationSnapshot empty() {
      return new PublicVerificationSnapshot(null, List.of());
    }

    public boolean isVisibleOnPublicTrace() {
      return lastVerified != null && !evidence.isEmpty();
    }
  }
}
