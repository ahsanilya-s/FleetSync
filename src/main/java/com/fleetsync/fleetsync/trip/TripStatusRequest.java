package com.fleetsync.fleetsync.trip;

import jakarta.validation.constraints.NotBlank;

/**
 * TripStatusRequest — represents the JSON body sent when updating a trip's status.
 *
 * Used by both MANAGER and DRIVER roles (see SecurityConfig for access rules).
 * The controller validates that the requested status is a valid transition
 * from the trip's current status.
 *
 * Example JSON body:
 * { "status": "IN_PROGRESS" }
 *
 * Valid status values: "IN_PROGRESS", "COMPLETED", "CANCELLED"
 * (case-insensitive — normalized to uppercase in TripController)
 */
public record TripStatusRequest(

    /**
     * The new status to apply to the trip.
     * Must be a valid transition from the current status (enforced in TripController).
     * @NotBlank — must not be null or empty
     */
    @NotBlank
    String status
) {}
