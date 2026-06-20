package com.farmtofolk.farmtofolk_ledger.verification;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class VerificationEvidenceController {

    private final VerificationEvidenceService verificationEvidenceService;

    public VerificationEvidenceController(VerificationEvidenceService verificationEvidenceService) {
        this.verificationEvidenceService = verificationEvidenceService;
    }

    @PostMapping("/api/verifications/{verificationId}/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationEvidenceResponse createVerificationEvidence(
            @PathVariable UUID verificationId,
            @RequestBody CreateVerificationEvidenceRequest request
    ) {
        return verificationEvidenceService.createVerificationEvidence(verificationId, request);
    }

    @GetMapping("/api/verifications/{verificationId}/evidence")
    public List<VerificationEvidenceResponse> getEvidenceForVerification(@PathVariable UUID verificationId) {
        return verificationEvidenceService.getEvidenceForVerification(verificationId);
    }

    @DeleteMapping("/api/evidence/{evidenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvidence(@PathVariable UUID evidenceId) {
        verificationEvidenceService.deleteEvidence(evidenceId);
    }
}
