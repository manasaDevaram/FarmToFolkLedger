package com.farmtofolk.farmtofolk_ledger.qr;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QrCodeController {

  private final QrCodeService qrCodeService;

  public QrCodeController(QrCodeService qrCodeService) {
    this.qrCodeService = qrCodeService;
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
}
