package com.farmtofolk.farmtofolk_ledger.traceability;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.TraceEventCreatedEvent;
import java.util.List;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TraceEventService {

  private static final List<String> DEFAULT_EVENT_TYPES =
      List.of(
          "SOWN",
          "GROWING",
          "READY_FOR_HARVEST",
          "HARVESTED",
          "RECEIVED",
          "CLEANED",
          "GRADED",
          "PACKED",
          "VERIFIED",
          "SHIPPED",
          "RECEIVED_AT_MARKET",
          "READY_FOR_SALE",
          "SOLD");

  private final TraceEventRepository traceEventRepository;
  private final BatchRepository batchRepository;
  private final DomainEventPublisher domainEventPublisher;

  public TraceEventService(
      TraceEventRepository traceEventRepository,
      BatchRepository batchRepository,
      DomainEventPublisher domainEventPublisher) {
    this.traceEventRepository = traceEventRepository;
    this.batchRepository = batchRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

  public TraceEventResponse createTraceEvent(UUID batchId, CreateTraceEventRequest request) {
    // Make sure the trace event is linked to a real batch.
    verifyBatchExists(batchId);
    // Copy request data into a new TraceEvent entity.
    TraceEvent traceEvent = new TraceEvent();
    traceEvent.setBatchId(batchId);
    applyRequest(traceEvent, request);

    // Save the trace event and return API-friendly response data.
    TraceEvent savedTraceEvent = traceEventRepository.save(traceEvent);
    updateCurrentBatchStatus(batchId, savedTraceEvent);
    domainEventPublisher.publishAfterCommit(
        new TraceEventCreatedEvent(batchId, savedTraceEvent.getId()));
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

  public List<String> getEventTypes() {
    LinkedHashSet<String> eventTypes = new LinkedHashSet<>(DEFAULT_EVENT_TYPES);
    traceEventRepository.findAll().stream()
        .map(TraceEvent::getEventType)
        .filter(type -> type != null && !type.isBlank())
        .forEach(eventTypes::add);
    return eventTypes.stream().sorted(Comparator.naturalOrder()).toList();
  }

  private void verifyBatchExists(UUID batchId) {
    // Prevent creating or listing trace events for batches that do not exist.
    if (!batchRepository.existsById(batchId)) {
      throw new ResourceNotFoundException("Batch not found");
    }
  }

  private void applyRequest(TraceEvent traceEvent, CreateTraceEventRequest request) {
    // Keep request-to-entity field mapping in one place.
    traceEvent.setEventType(normalizeEventType(request.eventType()));
    traceEvent.setEventTime(request.eventTime());
    traceEvent.setLocation(request.location());
    traceEvent.setDescription(request.description());
    traceEvent.setActorUserId(request.actorUserId());
    traceEvent.setMetadataJson(request.metadataJson());
  }

  private String normalizeEventType(String eventType) {
    return eventType.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "")
        .toUpperCase(Locale.ROOT);
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
