package com.farmtofolk.farmtofolk_ledger.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundReturns404Body() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/farmers/missing");

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Farmer not found"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Farmer not found", response.getBody().message());
        assertEquals("/api/farmers/missing", response.getBody().path());
    }

    @Test
    void badRequestReturns400Body() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/batches/id/trace-events");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new BadRequestException("Invalid trace event type"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid trace event type", response.getBody().message());
    }
}
