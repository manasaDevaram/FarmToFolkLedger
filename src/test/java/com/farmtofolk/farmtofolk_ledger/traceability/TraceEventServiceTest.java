package com.farmtofolk.farmtofolk_ledger.traceability;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.util.List;
import java.util.Optional;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceEventServiceTest {

  @Mock private TraceEventRepository traceEventRepository;

  @Mock private BatchRepository batchRepository;

  @Mock private PublicTraceCacheService publicTraceCacheService;

  @Mock private AfterCommitExecutor afterCommitExecutor;

  @InjectMocks private TraceEventService traceEventService;

  @Test
  void createTraceEventRejectsUnsupportedEventType() {
    UUID batchId = UUID.randomUUID();
    when(batchRepository.existsById(batchId)).thenReturn(true);

    CreateTraceEventRequest request =
        new CreateTraceEventRequest("IN_TRANSIT", LocalDateTime.now(), null, null, null, null);

    assertThrows(
        BadRequestException.class, () -> traceEventService.createTraceEvent(batchId, request));
  }

  @Test
  void createTraceEventUpdatesBatchCurrentStatus() {
    UUID batchId = UUID.randomUUID();
    Batch batch = new Batch();
    CreateTraceEventRequest request =
        new CreateTraceEventRequest("PACKED", LocalDateTime.now(), null, null, null, null);
    when(batchRepository.existsById(batchId)).thenReturn(true);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(traceEventRepository.save(org.mockito.ArgumentMatchers.any(TraceEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId)).thenReturn(List.of());

    traceEventService.createTraceEvent(batchId, request);

    verify(batchRepository).save(batch);
    org.junit.jupiter.api.Assertions.assertEquals("PACKED", batch.getStatus());
  }
}
