package com.farmtofolk.farmtofolk_ledger.qr;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.QrCodeCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private QrCodeRepository qrCodeRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    void qrCreationPublishesQrCodeCreatedEvent() {
        UUID batchId = UUID.randomUUID();
        UUID qrCodeId = UUID.randomUUID();
        when(batchRepository.existsById(batchId)).thenReturn(true);
        when(qrCodeRepository.findFirstByBatchIdAndIsActiveTrue(batchId)).thenReturn(Optional.empty());
        when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(invocation -> {
            QrCode qrCode = invocation.getArgument(0);
            ReflectionTestUtils.setField(qrCode, "id", qrCodeId);
            return qrCode;
        });

        QrCodeResponse response = qrCodeService.createQrCode(batchId);

        verify(domainEventPublisher).publishAfterCommit(
                new QrCodeCreatedEvent(qrCodeId, batchId, response.publicToken())
        );
    }
}
