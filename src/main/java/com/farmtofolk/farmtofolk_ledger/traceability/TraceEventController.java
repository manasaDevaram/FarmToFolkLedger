package com.farmtofolk.farmtofolk_ledger.traceability;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TraceEventController {

  private final TraceEventService traceEventService;

  public TraceEventController(TraceEventService traceEventService) {
    this.traceEventService = traceEventService;
  }

  @PostMapping("/api/batches/{batchId}/trace-events")
  @ResponseStatus(HttpStatus.CREATED)
  public TraceEventResponse createTraceEvent(
      @PathVariable UUID batchId, @Valid @RequestBody CreateTraceEventRequest request) {
    return traceEventService.createTraceEvent(batchId, request);
  }

  @GetMapping("/api/batches/{batchId}/trace-events")
  public List<TraceEventResponse> getTraceEventsForBatch(@PathVariable UUID batchId) {
    return traceEventService.getTraceEventsForBatch(batchId);
  }
}
