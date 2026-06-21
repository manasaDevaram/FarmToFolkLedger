package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.analytics.ScanEventService;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaRepository;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownResponse;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicTraceService {

    private final QrCodeRepository qrCodeRepository;
    private final BatchRepository batchRepository;
    private final FarmerRepository farmerRepository;
    private final FarmRepository farmRepository;
    private final FarmVerificationRepository farmVerificationRepository;
    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final FarmMediaRepository farmMediaRepository;
    private final PriceBreakdownRepository priceBreakdownRepository;
    private final TraceEventRepository traceEventRepository;
    private final ScanEventService scanEventService;

    public PublicTraceService(
            QrCodeRepository qrCodeRepository,
            BatchRepository batchRepository,
            FarmerRepository farmerRepository,
            FarmRepository farmRepository,
            FarmVerificationRepository farmVerificationRepository,
            VerificationEvidenceRepository verificationEvidenceRepository,
            FarmMediaRepository farmMediaRepository,
            PriceBreakdownRepository priceBreakdownRepository,
            TraceEventRepository traceEventRepository,
            ScanEventService scanEventService
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.batchRepository = batchRepository;
        this.farmerRepository = farmerRepository;
        this.farmRepository = farmRepository;
        this.farmVerificationRepository = farmVerificationRepository;
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.farmMediaRepository = farmMediaRepository;
        this.priceBreakdownRepository = priceBreakdownRepository;
        this.traceEventRepository = traceEventRepository;
        this.scanEventService = scanEventService;
    }

    public PublicTraceResponse getPublicTrace(String publicToken) {
        // Best-effort scan recording should never block the public trace response.
        try {
            scanEventService.recordScan(publicToken, null, null, null, null, null, null);
        } catch (RuntimeException ignored) {
        }

        // Resolve the public token to an active QR code.
        QrCode qrCode = qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        // Load the required core trace objects.
        Batch batch = batchRepository.findById(qrCode.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        Farmer farmer = farmerRepository.findById(batch.getFarmerId())
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        Farm farm = farmRepository.findById(batch.getFarmId())
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        // Load optional verification and evidence data when available.
        FarmVerification latestVerification = farmVerificationRepository
                .findFirstByFarmIdOrderByVerificationDateDesc(farm.getId())
                .orElse(null);
        List<VerificationEvidenceResponse> verificationEvidence = latestVerification == null
                ? List.of()
                : verificationEvidenceRepository
                        .findByVerificationIdOrderByCreatedAtAsc(latestVerification.getId())
                        .stream()
                        .map(VerificationEvidenceResponse::from)
                        .toList();

        // Load optional media, price, and trace event data.
        List<FarmMediaResponse> farmMedia = farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farm.getId())
                .stream()
                .map(FarmMediaResponse::from)
                .toList();
        PriceBreakdownResponse priceBreakdown = priceBreakdownRepository.findByBatchId(batch.getId())
                .map(PriceBreakdownResponse::from)
                .orElse(null);
        List<TraceEventResponse> traceEvents = traceEventRepository.findByBatchIdOrderByEventTimeAsc(batch.getId())
                .stream()
                .map(TraceEventResponse::from)
                .toList();

        return new PublicTraceResponse(
                QrCodeResponse.from(qrCode),
                BatchResponse.from(batch),
                FarmerResponse.from(farmer),
                FarmResponse.from(farm),
                latestVerification == null ? null : FarmVerificationResponse.from(latestVerification),
                verificationEvidence,
                farmMedia,
                priceBreakdown,
                traceEvents
        );
    }
}
