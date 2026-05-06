package com.fleetsync.fleetsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus tells Spring: whenever this exception is thrown and NOT caught by a
// global handler, automatically send an HTTP 400 Bad Request response to the client.
// We use this for business rule violations that are not about missing or duplicate data.
// Example: trying to assign a RETIRED vehicle to a new trip, or starting an already
// COMPLETED trip — the data exists, but the operation is logically not allowed.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    // Constructor accepts a custom message describing the violated business rule.
    // Example usage: throw new BusinessException("Cannot assign a retired vehicle to a trip")
    // 'super(message)' passes the message up to RuntimeException so it can be retrieved
    // later via ex.getMessage() inside the global exception handler.
    public BusinessException(String message) {
        super(message);
    }
}
