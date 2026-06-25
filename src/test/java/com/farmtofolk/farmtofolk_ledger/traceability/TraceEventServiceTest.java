package com.farmtofolk.farmtofolk_ledger.traceability;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceEventServiceTest {

    @Mock
    private TraceEventRepository traceEventRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private TraceEventService traceEventService;

    @Test
    void createTraceEventRejectsUnsupportedEventType() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.existsById(batchId)).thenReturn(true);

        CreateTraceEventRequest request = new CreateTraceEventRequest(
                "IN_TRANSIT",
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );

        assertThrows(BadRequestException.class, () -> traceEventService.createTraceEvent(batchId, request));
    }
}
