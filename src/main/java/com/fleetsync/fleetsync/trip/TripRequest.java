package com.fleetsync.fleetsync.trip;

import jakarta.validation.constraints.*;

// Represents the JSON body sent by the manager when assigning a driver to a vehicle for a trip
public record TripRequest(

    // ID of the driver being assigned — must exist in the drivers table
    @NotNull
    Long driverId,

    // ID of the vehicle being assigned — must exist in the vehicles table
    @NotNull
    Long vehicleId,

    @NotBlank
    @Size(max = 255)
    String origin,

    @NotBlank
    @Size(max = 255)
    String destination
) {}
