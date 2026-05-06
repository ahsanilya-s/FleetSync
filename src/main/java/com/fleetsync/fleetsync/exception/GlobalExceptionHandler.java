package com.fleetsync.fleetsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice marks this class as a GLOBAL exception handler.
// Instead of handling exceptions inside every controller separately, Spring
// automatically routes ALL unhandled exceptions from ANY controller into this
// single class. This keeps controllers clean and error responses consistent.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler tells Spring: "when a ResourceNotFoundException is thrown
    // anywhere in the application, run THIS method instead of crashing."
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        // Delegate to our shared build() helper, passing the 404 status and the
        // exception message (e.g. "Vehicle not found with id: 5").
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Handles DuplicateResourceException — thrown when a client tries to create
    // a resource that already exists (e.g. duplicate plate number).
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        // Returns HTTP 409 Conflict with the exception message.
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Handles BusinessException — thrown when a business rule is violated
    // (e.g. assigning a RETIRED vehicle to a trip).
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        // Returns HTTP 400 Bad Request with the exception message.
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Handles MethodArgumentNotValidException — this is thrown automatically by Spring
    // when a request body fails @Valid / @NotBlank / @NotNull / @Email etc. validation.
    // This gives the client a detailed breakdown of WHICH fields failed and WHY.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        // Create a map to collect each field name and its validation error message.
        // Example entry: { "plateNumber": "Plate number is required" }
        Map<String, String> fieldErrors = new HashMap<>();

        // ex.getBindingResult().getAllErrors() returns a list of all validation failures.
        // We loop through each one to extract the field name and the error message.
        ex.getBindingResult().getAllErrors().forEach(err -> {
            // Cast the generic ObjectError to FieldError to access the specific field name.
            String field = ((FieldError) err).getField();

            // getDefaultMessage() returns the message we wrote in the annotation,
            // e.g. @NotBlank(message = "Plate number is required") → "Plate number is required"
            fieldErrors.put(field, err.getDefaultMessage());
        });

        // Build the full response body with timestamp, status, label, and the field errors map.
        Map<String, Object> resp = new HashMap<>();
        resp.put("timestamp", LocalDateTime.now());  // When the error occurred
        resp.put("status", 400);                     // HTTP status code as an integer
        resp.put("error", "Validation Failed");       // Human-readable error label
        resp.put("details", fieldErrors);             // The map of field → error message

        // Return HTTP 400 Bad Request with the structured response body.
        return ResponseEntity.badRequest().body(resp);
    }

    // -------------------------------------------------------------------------
    // Private helper method used by all handlers above.
    // Builds a consistent JSON error response body so every error looks the same.
    // Example response body:
    // {
    //   "timestamp": "2025-01-15T10:30:00",
    //   "status": 404,
    //   "error": "Not Found",
    //   "message": "Vehicle not found with id: 5"
    // }
    // -------------------------------------------------------------------------
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());      // Capture exact time of the error
        body.put("status", status.value());              // e.g. 404, 409, 400 as an integer
        body.put("error", status.getReasonPhrase());     // e.g. "Not Found", "Conflict", "Bad Request"
        body.put("message", message);                    // The specific message from the thrown exception

        // Wrap the body map in a ResponseEntity so Spring sets the correct HTTP status code
        // on the actual HTTP response sent back to the client.
        return ResponseEntity.status(status).body(body);
    }
}
