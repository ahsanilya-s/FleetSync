package com.fleetsync.fleetsync.trip;

import java.time.LocalDateTime;

// Represents a trip assignment record as returned from the database
// Links a driver to a vehicle for a specific journey
public record Trip(
    Long id,
    Long driverId,         // FK → drivers.id
    Long vehicleId,        // FK → vehicles.id
    String origin,
    String destination,
    String status,         // SCHEDULED, IN_PROGRESS, COMPLETED
    LocalDateTime createdAt
) {}
