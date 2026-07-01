package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.events.QrCodeCreatedEvent;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrImageGenerationService {

    private static final int QR_IMAGE_SIZE = 512;

    private final StorageService storageService;
    private final QrImagePersistenceService qrImagePersistenceService;
    private final String publicTraceBaseUrl;

    public QrImageGenerationService(
            StorageService storageService,
            QrImagePersistenceService qrImagePersistenceService,
            @Value("${app.public-trace-base-url:http://localhost:8080/api/public/trace}") String publicTraceBaseUrl
    ) {
        this.storageService = storageService;
        this.qrImagePersistenceService = qrImagePersistenceService;
        this.publicTraceBaseUrl = publicTraceBaseUrl.replaceAll("/+$", "");
    }

    public void generateAndUpload(QrCodeCreatedEvent event) {
        byte[] png = generatePng(publicTraceBaseUrl + "/" + event.publicToken());
        StoredFileResponse storedFile = storageService.upload(
                png,
                event.publicToken() + ".png",
                "image/png",
                "qr-codes/" + event.batchId()
        );

        qrImagePersistenceService.updateImageUrl(event.qrCodeId(), storedFile.fileUrl());
    }

    private byte[] generatePng(String value) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    value,
                    BarcodeFormat.QR_CODE,
                    QR_IMAGE_SIZE,
                    QR_IMAGE_SIZE
            );
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("QR image generation failed", exception);
        }
    }
}
