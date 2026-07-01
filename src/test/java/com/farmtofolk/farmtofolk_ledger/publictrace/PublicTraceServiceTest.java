package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.PublicTraceScannedEvent;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicTraceServiceTest {

    @Mock
    private QrCodeRepository qrCodeRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private TraceEventRepository traceEventRepository;
    @Mock
    private PublicTraceCacheService publicTraceCacheService;
    @Mock
    private DomainEventPublisher domainEventPublisher;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private PublicTraceService publicTraceService;

    @Test
    void publicTraceReturnsBeforeScanRecordingAndOnlyPublishesEvent() {
        String token = "public-token";
        UUID batchId = UUID.randomUUID();
        QrCode qrCode = new QrCode();
        qrCode.setBatchId(batchId);
        qrCode.setPublicToken(token);
        qrCode.setIsActive(true);

        Batch batch = new Batch();
        ReflectionTestUtils.setField(batch, "id", batchId);
        FarmerResponse farmer = new FarmerResponse(
                UUID.randomUUID(), "F-1", "Farmer", null, null, null, null,
                null, null, null, null, true, null, null);
        CachedPublicTraceStableData stableData = new CachedPublicTraceStableData(
                BatchResponse.from(batch), farmer, null, null, List.of(), List.of()
        );

        when(qrCodeRepository.findByPublicTokenAndIsActiveTrue(token)).thenReturn(Optional.of(qrCode));
        when(publicTraceCacheService.getStableData(token)).thenReturn(stableData);
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId)).thenReturn(List.of());

        PublicTraceResponse response = publicTraceService.getPublicTrace(token);

        assertNotNull(response);
        verify(domainEventPublisher).publishAfterCommit(
                new PublicTraceScannedEvent(token, null, null, null, null, null, null)
        );
    }
}
