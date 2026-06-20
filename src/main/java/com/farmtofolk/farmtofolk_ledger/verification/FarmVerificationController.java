package com.farmtofolk.farmtofolk_ledger.verification;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class FarmVerificationController {

    private final FarmVerificationService farmVerificationService;

    public FarmVerificationController(FarmVerificationService farmVerificationService) {
        this.farmVerificationService = farmVerificationService;
    }

    @PostMapping("/api/farms/{farmId}/verifications")
    @ResponseStatus(HttpStatus.CREATED)
    public FarmVerificationResponse createFarmVerification(
            @PathVariable UUID farmId,
            @RequestBody CreateFarmVerificationRequest request
    ) {
        return farmVerificationService.createFarmVerification(farmId, request);
    }

    @GetMapping("/api/farms/{farmId}/verifications")
    public List<FarmVerificationResponse> getFarmVerifications(@PathVariable UUID farmId) {
        return farmVerificationService.getFarmVerifications(farmId);
    }

    @GetMapping("/api/farms/{farmId}/latest-verification")
    public FarmVerificationResponse getLatestFarmVerification(@PathVariable UUID farmId) {
        return farmVerificationService.getLatestFarmVerification(farmId);
    }

    @GetMapping("/api/verifications/{verificationId}")
    public FarmVerificationResponse getFarmVerification(@PathVariable UUID verificationId) {
        return farmVerificationService.getFarmVerification(verificationId);
    }

    @PutMapping("/api/verifications/{verificationId}")
    public FarmVerificationResponse updateFarmVerification(
            @PathVariable UUID verificationId,
            @RequestBody CreateFarmVerificationRequest request
    ) {
        return farmVerificationService.updateFarmVerification(verificationId, request);
    }
}
