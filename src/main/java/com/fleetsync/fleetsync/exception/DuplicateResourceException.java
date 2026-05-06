package com.fleetsync.fleetsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus tells Spring: whenever this exception is thrown and NOT caught by a
// global handler, automatically send an HTTP 409 Conflict response to the client.
// We use this when a client tries to create a resource that already exists.
// Example: registering a vehicle with a plate number that is already in the database.
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    // Constructor accepts a custom message describing the duplicate.
    // Example usage: throw new DuplicateResourceException("Plate number ABC-123 already exists")
    // 'super(message)' passes the message up to RuntimeException so it can be retrieved
    // later via ex.getMessage() inside the global exception handler.
    public DuplicateResourceException(String message) {
        super(message);
    }
}
