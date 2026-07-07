package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
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

  public QrCodeResponse createQrCode(UUID batchId) {
    // Make sure the QR code is linked to a real batch.
    verifyBatchExists(batchId);

    // Return the existing active QR code if this batch already has one.
    return qrCodeRepository
        .findFirstByBatchIdAndIsActiveTrue(batchId)
        .map(
            qrCode -> {
              // POST is idempotent, but also acts as a retry while image generation is pending.
              if (qrCode.getQrImageUrl() == null || qrCode.getQrImageUrl().isBlank()) {
                publishGeneration(qrCode);
              }
              return QrCodeResponse.from(qrCode, storageService);
            })
        .orElseGet(() -> createNewQrCode(batchId));
  }

  public QrCodeResponse getQrCode(UUID batchId) {
    // Make sure the batch exists before reading its active QR code.
    verifyBatchExists(batchId);

    // Return the active QR code for this batch.
    QrCode qrCode =
        qrCodeRepository
            .findFirstByBatchIdAndIsActiveTrue(batchId)
            .orElseThrow(
                () -> new ResourceNotFoundException("No active QR code found for this batch"));
    return QrCodeResponse.from(qrCode, storageService);
  }

  private QrCodeResponse createNewQrCode(UUID batchId) {
    // Build a new consumer QR code with a random public token.
    QrCode qrCode = new QrCode();
    qrCode.setBatchId(batchId);
    qrCode.setPublicToken(UUID.randomUUID().toString());
    qrCode.setQrType(CONSUMER_QR_TYPE);
    qrCode.setIsActive(true);
    qrCode.setGeneratedAt(LocalDateTime.now());

    QrCode savedQrCode = qrCodeRepository.save(qrCode);
    publishGeneration(savedQrCode);
    return QrCodeResponse.from(savedQrCode, storageService);
  }

  private void publishGeneration(QrCode qrCode) {
    domainEventPublisher.publishAfterCommit(
        new QrCodeCreatedEvent(qrCode.getId(), qrCode.getBatchId(), qrCode.getPublicToken()));
  }

  private void verifyBatchExists(UUID batchId) {
    // Prevent creating or reading QR codes for batches that do not exist.
    if (!batchRepository.existsById(batchId)) {
      throw new ResourceNotFoundException("Batch not found");
    }
  }
}
