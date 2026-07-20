package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.BatchUpdatedEvent;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.QrCodeCreatedEvent;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QrCodeService {

  private static final String CONSUMER_QR_TYPE = "CONSUMER";

  private final QrCodeRepository qrCodeRepository;
  private final BatchRepository batchRepository;
  private final DomainEventPublisher domainEventPublisher;
  private final StorageService storageService;

  public QrCodeService(
      QrCodeRepository qrCodeRepository,
      BatchRepository batchRepository,
      DomainEventPublisher domainEventPublisher,
      StorageService storageService) {
    this.qrCodeRepository = qrCodeRepository;
    this.batchRepository = batchRepository;
    this.domainEventPublisher = domainEventPublisher;
    this.storageService = storageService;
  }

  public QrCodeResponse createQrCodeForSowing(UUID sowingBatchId) {
    verifyBatchExists(sowingBatchId);

    return qrCodeRepository
        .findFirstBySowingBatchIdAndIsActiveTrue(sowingBatchId)
        .map(this::ensureGenerated)
        .orElseGet(() -> createNewQrCode(sowingBatchId, sowingBatchId));
  }

  public QrCodeResponse createQrCode(UUID batchId) {
    return createQrCodeForSowing(batchId);
  }

  public void pointQrToProcuredBatch(UUID sowingBatchId, UUID procuredBatchId) {
    verifyBatchExists(procuredBatchId);
    QrCode qrCode =
        qrCodeRepository
            .findFirstBySowingBatchIdAndIsActiveTrue(sowingBatchId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No active QR code found for sowing batch " + sowingBatchId));
    qrCode.setBatchId(procuredBatchId);
    qrCodeRepository.save(qrCode);
    domainEventPublisher.publishAfterCommit(new BatchUpdatedEvent(procuredBatchId));
  }

  public QrCodeResponse getQrCode(UUID batchId) {
    verifyBatchExists(batchId);
    Batch batch =
        batchRepository
            .findById(batchId)
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    UUID sowingBatchId = resolveSowingBatchId(batch);

    QrCode qrCode =
        qrCodeRepository
            .findFirstBySowingBatchIdAndIsActiveTrue(sowingBatchId)
            .or(() -> qrCodeRepository.findFirstByBatchIdAndIsActiveTrue(batchId))
            .orElseThrow(
                () -> new ResourceNotFoundException("No active QR code found for this batch"));
    return QrCodeResponse.from(qrCode, storageService);
  }

  private QrCodeResponse ensureGenerated(QrCode qrCode) {
    if (qrCode.getQrImageUrl() == null || qrCode.getQrImageUrl().isBlank()) {
      publishGeneration(qrCode);
    }
    return QrCodeResponse.from(qrCode, storageService);
  }

  private QrCodeResponse createNewQrCode(UUID sowingBatchId, UUID activeBatchId) {
    QrCode qrCode = new QrCode();
    qrCode.setBatchId(activeBatchId);
    qrCode.setSowingBatchId(sowingBatchId);
    qrCode.setPublicToken(UUID.randomUUID().toString());
    qrCode.setQrType(CONSUMER_QR_TYPE);
    qrCode.setIsActive(true);
    qrCode.setGeneratedAt(LocalDateTime.now());

    QrCode savedQrCode = qrCodeRepository.save(qrCode);
    publishGeneration(savedQrCode);
    return QrCodeResponse.from(savedQrCode, storageService);
  }

  private UUID resolveSowingBatchId(Batch batch) {
    if (batch.getParentBatchId() != null) {
      return batch.getParentBatchId();
    }
    return batch.getId();
  }

  private void publishGeneration(QrCode qrCode) {
    domainEventPublisher.publishAfterCommit(
        new QrCodeCreatedEvent(qrCode.getId(), qrCode.getBatchId(), qrCode.getPublicToken()));
  }

  private void verifyBatchExists(UUID batchId) {
    if (!batchRepository.existsById(batchId)) {
      throw new ResourceNotFoundException("Batch not found");
    }
  }
}
