package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.analytics.ScanEventService;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownResponse;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicTraceService {

    private final QrCodeRepository qrCodeRepository;
    private final BatchRepository batchRepository;
    private final PriceBreakdownRepository priceBreakdownRepository;
    private final TraceEventRepository traceEventRepository;
    private final ScanEventService scanEventService;
    private final PublicTraceCacheService publicTraceCacheService;

    public PublicTraceService(
            QrCodeRepository qrCodeRepository,
            BatchRepository batchRepository,
            PriceBreakdownRepository priceBreakdownRepository,
            TraceEventRepository traceEventRepository,
            ScanEventService scanEventService,
            PublicTraceCacheService publicTraceCacheService
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.batchRepository = batchRepository;
        this.priceBreakdownRepository = priceBreakdownRepository;
        this.traceEventRepository = traceEventRepository;
        this.scanEventService = scanEventService;
        this.publicTraceCacheService = publicTraceCacheService;
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

        // Load stable page sections through Redis cache with PostgreSQL fallback.
        CachedPublicTraceStableData stableData = publicTraceCacheService.getStableData(publicToken);

        // Load the batch live only to anchor frequently changing sections to the QR batch ID.
        Batch batch = batchRepository.findById(qrCode.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // Always load price and trace events live because they change more often.
        PriceBreakdownResponse priceBreakdown = priceBreakdownRepository.findByBatchId(batch.getId())
                .map(PriceBreakdownResponse::from)
                .orElse(null);
        List<TraceEventResponse> traceEvents = traceEventRepository.findByBatchIdOrderByEventTimeAsc(batch.getId())
                .stream()
                .map(TraceEventResponse::from)
                .toList();
        long scanCount = scanEventService.getScanCount(qrCode.getId());

        return new PublicTraceResponse(
                QrCodeResponse.from(qrCode),
                stableData.batch(),
                stableData.farmer(),
                stableData.farm(),
                stableData.latestVerification(),
                stableData.verificationEvidence(),
                stableData.farmMedia(),
                priceBreakdown,
                traceEvents,
                scanCount
        );
    }
}
