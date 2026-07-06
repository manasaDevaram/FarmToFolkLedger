package com.farmtofolk.farmtofolk_ledger.publictrace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.QrScannedEvent;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PublicTraceServiceTest {

  @Mock private QrCodeRepository qrCodeRepository;
  @Mock private PublicTraceCacheService publicTraceCacheService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private PublicTraceService publicTraceService;

  @Test
  void publishesResolvedQrDetailsAndReturnsCachedResponse() {
    String token = "public-token";
    UUID qrCodeId = UUID.randomUUID();
    UUID batchId = UUID.randomUUID();
    QrCode qrCode = new QrCode();
    org.springframework.test.util.ReflectionTestUtils.setField(qrCode, "id", qrCodeId);
    qrCode.setBatchId(batchId);
    qrCode.setPublicToken(token);
    qrCode.setIsActive(true);
    PublicTraceResponse cachedResponse =
        new PublicTraceResponse(null, null, null, null, null, List.of(), List.of(), List.of());
    when(qrCodeRepository.findByPublicToken(token)).thenReturn(Optional.of(qrCode));
    when(publicTraceCacheService.getFullTrace(token, qrCode)).thenReturn(cachedResponse);

    assertEquals(cachedResponse, publicTraceService.getPublicTrace(token));

    ArgumentCaptor<QrScannedEvent> captor = ArgumentCaptor.forClass(QrScannedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertEquals(qrCodeId, captor.getValue().qrCodeId());
    assertEquals(batchId, captor.getValue().batchId());
    verify(publicTraceCacheService).getFullTrace(token, qrCode);
  }

  @Test
  void invalidTokenIsNotPublishedOrCached() {
    String token = "invalid-token";
    when(qrCodeRepository.findByPublicToken(token)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> publicTraceService.getPublicTrace(token));

    verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    verify(publicTraceCacheService, never())
        .getFullTrace(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void eventPublicationFailureDoesNotFailPublicTrace() {
    String token = "public-token";
    QrCode qrCode = new QrCode();
    org.springframework.test.util.ReflectionTestUtils.setField(qrCode, "id", UUID.randomUUID());
    qrCode.setBatchId(UUID.randomUUID());
    qrCode.setPublicToken(token);
    qrCode.setIsActive(true);
    PublicTraceResponse cachedResponse =
        new PublicTraceResponse(null, null, null, null, null, List.of(), List.of(), List.of());
    when(qrCodeRepository.findByPublicToken(token)).thenReturn(Optional.of(qrCode));
    when(publicTraceCacheService.getFullTrace(token, qrCode)).thenReturn(cachedResponse);
    doThrow(new IllegalStateException("executor full"))
        .when(eventPublisher)
        .publishEvent(org.mockito.ArgumentMatchers.any(QrScannedEvent.class));

    assertEquals(cachedResponse, publicTraceService.getPublicTrace(token));
  }
}
