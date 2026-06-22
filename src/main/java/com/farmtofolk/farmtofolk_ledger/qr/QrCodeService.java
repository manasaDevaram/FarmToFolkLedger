package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class QrCodeService {

    private static final String CONSUMER_QR_TYPE = "CONSUMER";

    private final QrCodeRepository qrCodeRepository;
    private final BatchRepository batchRepository;

    public QrCodeService(QrCodeRepository qrCodeRepository, BatchRepository batchRepository) {
        this.qrCodeRepository = qrCodeRepository;
        this.batchRepository = batchRepository;
    }

    public QrCodeResponse createQrCode(UUID batchId) {
        // Make sure the QR code is linked to a real batch.
        verifyBatchExists(batchId);

        // Return the existing active QR code if this batch already has one.
        return qrCodeRepository.findFirstByBatchIdAndIsActiveTrue(batchId)
                .map(QrCodeResponse::from)
                .orElseGet(() -> createNewQrCode(batchId));
    }

    public QrCodeResponse getQrCode(UUID batchId) {
        // Make sure the batch exists before reading its active QR code.
        verifyBatchExists(batchId);

        // Return the active QR code for this batch.
        QrCode qrCode = qrCodeRepository.findFirstByBatchIdAndIsActiveTrue(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));
        return QrCodeResponse.from(qrCode);
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
        return QrCodeResponse.from(savedQrCode);
    }

    private void verifyBatchExists(UUID batchId) {
        // Prevent creating or reading QR codes for batches that do not exist.
        if (!batchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Batch not found");
        }
    }
}
