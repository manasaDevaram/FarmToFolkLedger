package com.farmtofolk.farmtofolk_ledger.qr;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.TimeUnit;

@RestController
public class QrCodeController {

  private final QrCodeService qrCodeService;
  private final QrCodeRepository qrCodeRepository;
  private final QrImageGenerationService qrImageGenerationService;

  public QrCodeController(
      QrCodeService qrCodeService,
      QrCodeRepository qrCodeRepository,
      QrImageGenerationService qrImageGenerationService) {
    this.qrCodeService = qrCodeService;
    this.qrCodeRepository = qrCodeRepository;
    this.qrImageGenerationService = qrImageGenerationService;
  }

  @PostMapping("/api/batches/{batchId}/qr-code")
  @ResponseStatus(HttpStatus.CREATED)
  public QrCodeResponse createQrCode(@PathVariable UUID batchId) {
    return qrCodeService.createQrCode(batchId);
  }

  @GetMapping("/api/batches/{batchId}/qr-code")
  public QrCodeResponse getQrCode(@PathVariable UUID batchId) {
    return qrCodeService.getQrCode(batchId);
  }

  @GetMapping(value = "/api/public/qr/{publicToken}.png", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> getPublicQrImage(@PathVariable String publicToken) {
    qrCodeRepository
        .findByPublicTokenAndIsActiveTrue(publicToken)
        .orElseThrow(() -> new com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException("QR code not found"));
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
        .contentType(MediaType.IMAGE_PNG)
        .body(qrImageGenerationService.generateForPublicToken(publicToken));
  }
}
