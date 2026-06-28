package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaResponse;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import java.util.List;

public record AdminFarmOverviewResponse(
        FarmResponse farm,
        FarmerResponse farmer,
        List<FarmMediaResponse> media,
        FarmVerificationResponse latestVerification,
        List<VerificationEvidenceResponse> verificationEvidence,
        List<BatchResponse> batches) {
}
