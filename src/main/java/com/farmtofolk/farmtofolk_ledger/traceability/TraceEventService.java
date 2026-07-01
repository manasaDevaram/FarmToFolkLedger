package com.farmtofolk.farmtofolk_ledger.traceability;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TraceEventService {

  private static final Set<String> ALLOWED_EVENT_TYPES =
      Set.of(
          "HARVESTED",
          "CLEANED",
          "GRADED",
          "PACKED",
          "VERIFIED",
          "SHIPPED",
          "RECEIVED_AT_MARKET",
          "SOLD");

  private final TraceEventRepository traceEventRepository;
  private final BatchRepository batchRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final AfterCommitExecutor afterCommitExecutor;

  public TraceEventService(
      TraceEventRepository traceEventRepository,
      BatchRepository batchRepository,
      PublicTraceCacheService publicTraceCacheService,
      AfterCommitExecutor afterCommitExecutor) {
    this.traceEventRepository = traceEventRepository;
    this.batchRepository = batchRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.afterCommitExecutor = afterCommitExecutor;
  }

  public TraceEventResponse createTraceEvent(UUID batchId, CreateTraceEventRequest request) {
    // Make sure the trace event is linked to a real batch.
    verifyBatchExists(batchId);
    // Allow only the trace event types supported by the API right now.
    verifyAllowedEventType(request.eventType());

    // Copy request data into a new TraceEvent entity.
    TraceEvent traceEvent = new TraceEvent();
    traceEvent.setBatchId(batchId);
    applyRequest(traceEvent, request);

    // Save the trace event and return API-friendly response data.
    TraceEvent savedTraceEvent = traceEventRepository.save(traceEvent);
    updateCurrentBatchStatus(batchId, savedTraceEvent);
    afterCommitExecutor.run(() -> publicTraceCacheService.evictStableDataForBatch(batchId));
    return TraceEventResponse.from(savedTraceEvent);
  }

  public List<TraceEventResponse> getTraceEventsForBatch(UUID batchId) {
    // Make sure the batch exists before listing its trace events.
    verifyBatchExists(batchId);

    // Fetch trace events in timeline order and convert each one to a response.
    return traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId).stream()
        .map(TraceEventResponse::from)
        .toList();
  }

  private void verifyBatchExists(UUID batchId) {
    // Prevent creating or listing trace events for batches that do not exist.
    if (!batchRepository.existsById(batchId)) {
      throw new ResourceNotFoundException("Batch not found");
    }
  }

  private void verifyAllowedEventType(String eventType) {
    // Keep invalid trace event types out until we introduce enums later.
    if (!ALLOWED_EVENT_TYPES.contains(eventType)) {
      throw new BadRequestException("Invalid trace event type");
    }
  }

  private void applyRequest(TraceEvent traceEvent, CreateTraceEventRequest request) {
    // Keep request-to-entity field mapping in one place.
    traceEvent.setEventType(request.eventType());
    traceEvent.setEventTime(request.eventTime());
    traceEvent.setLocation(request.location());
    traceEvent.setDescription(request.description());
    traceEvent.setActorUserId(request.actorUserId());
    traceEvent.setMetadataJson(request.metadataJson());
  }

  private void updateCurrentBatchStatus(UUID batchId, TraceEvent newEvent) {
    boolean hasLaterEvent =
        traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId).stream()
            .filter(event -> !event.equals(newEvent))
            .anyMatch(event ->
                event.getEventTime() != null
                    && newEvent.getEventTime() != null
                    && event.getEventTime().isAfter(newEvent.getEventTime()));
    if (hasLaterEvent) return;

    Batch batch =
        batchRepository
            .findById(batchId)
            .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    batch.setStatus(newEvent.getEventType());
    batchRepository.save(batch);
  }
}
