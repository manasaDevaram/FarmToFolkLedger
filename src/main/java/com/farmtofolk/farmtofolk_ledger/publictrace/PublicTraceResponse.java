package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaResponse;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import java.util.List;

public record PublicTraceResponse(
    QrCodeResponse qrCode,
    PublicBatchTraceResponse batch,
    FarmerResponse farmer,
    FarmResponse farm,
    FarmVerificationResponse latestVerification,
    List<VerificationEvidenceResponse> verificationEvidence,
    List<FarmMediaResponse> farmMedia,
    List<TraceEventResponse> traceEvents) {}
