package com.farmtofolk.farmtofolk_ledger.traceability;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class TraceEventService {

    private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
            "HARVESTED",
            "PACKED",
            "RECEIVED_AT_MARKET",
            "SOLD"
    );

    private final TraceEventRepository traceEventRepository;
    private final BatchRepository batchRepository;

    public TraceEventService(TraceEventRepository traceEventRepository, BatchRepository batchRepository) {
        this.traceEventRepository = traceEventRepository;
        this.batchRepository = batchRepository;
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
        return TraceEventResponse.from(savedTraceEvent);
    }

    public List<TraceEventResponse> getTraceEventsForBatch(UUID batchId) {
        // Make sure the batch exists before listing its trace events.
        verifyBatchExists(batchId);

        // Fetch trace events in timeline order and convert each one to a response.
        return traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId)
                .stream()
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
}
