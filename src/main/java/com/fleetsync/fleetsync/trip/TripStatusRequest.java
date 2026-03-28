package com.fleetsync.fleetsync.trip;

import jakarta.validation.constraints.NotBlank;

// Represents the JSON body sent when updating a trip's status
public record TripStatusRequest(

    // New status for the trip — must be a valid transition value
    @NotBlank
    String status
) {}
