package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.PublicTraceScannedEvent;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicTraceService {

  private final QrCodeRepository qrCodeRepository;
  private final BatchRepository batchRepository;
  private final TraceEventRepository traceEventRepository;
  private final DomainEventPublisher domainEventPublisher;
  private final PublicTraceCacheService publicTraceCacheService;
  private final StorageService storageService;

  public PublicTraceService(
      QrCodeRepository qrCodeRepository,
      BatchRepository batchRepository,
      TraceEventRepository traceEventRepository,
      DomainEventPublisher domainEventPublisher,
      PublicTraceCacheService publicTraceCacheService,
      StorageService storageService) {
    this.qrCodeRepository = qrCodeRepository;
    this.batchRepository = batchRepository;
    this.traceEventRepository = traceEventRepository;
    this.domainEventPublisher = domainEventPublisher;
    this.publicTraceCacheService = publicTraceCacheService;
    this.storageService = storageService;
  }

  public PublicTraceResponse getPublicTrace(String publicToken) {
    // Resolve the public token to an active QR code.
    QrCode qrCode =
        qrCodeRepository
            .findByPublicTokenAndIsActiveTrue(publicToken)
            .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));

    // Load stable page sections through Redis cache with PostgreSQL fallback.
    CachedPublicTraceStableData stableData = publicTraceCacheService.getStableData(publicToken);

    // Load the batch live only to anchor frequently changing sections to the QR batch ID.
    Batch batch =
        batchRepository
            .findById(qrCode.getBatchId())
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

    // Always load price and trace events live because they change more often.
    List<TraceEventResponse> traceEvents =
        traceEventRepository.findByBatchIdOrderByEventTimeAsc(batch.getId()).stream()
            .map(TraceEventResponse::from)
            .toList();

    PublicTraceResponse response = new PublicTraceResponse(
        QrCodeResponse.from(qrCode),
        PublicBatchTraceResponse.from(stableData.batch()),
        stableData.farmer().withPresignedUrls(storageService),
        stableData.farm(),
        stableData.latestVerification(),
        stableData.verificationEvidence().stream()
            .map(evidence -> evidence.withPresignedUrl(storageService)).toList(),
        stableData.farmMedia().stream()
            .map(media -> media.withPresignedUrl(storageService)).toList(),
        traceEvents);
    domainEventPublisher.publishAfterCommit(
        new PublicTraceScannedEvent(publicToken, null, null, null, null, null, null));
    return response;
  }
}
