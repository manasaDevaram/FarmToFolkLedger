package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
            @Valid @RequestBody CreateVerificationEvidenceRequest request
    ) {
        return verificationEvidenceService.createVerificationEvidence(verificationId, request);
    }

    @PostMapping(
            value = "/api/verifications/{verificationId}/evidence/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationEvidenceResponse uploadVerificationEvidence(
            @PathVariable UUID verificationId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String caption
    ) {
        return verificationEvidenceService.uploadVerificationEvidence(verificationId, file, caption);
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
