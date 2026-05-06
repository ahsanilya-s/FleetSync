package com.fleetsync.fleetsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus tells Spring: whenever this exception is thrown and NOT caught by a
// global handler, automatically send an HTTP 404 Not Found response to the client.
// We use this when a requested resource (e.g. Vehicle, Driver, Trip) does not exist in the DB.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Constructor accepts a custom message describing what was not found.
    // Example usage: throw new ResourceNotFoundException("Vehicle not found with id: 5")
    // 'super(message)' passes the message up to RuntimeException so it can be retrieved
    // later via ex.getMessage() inside the global exception handler.
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
