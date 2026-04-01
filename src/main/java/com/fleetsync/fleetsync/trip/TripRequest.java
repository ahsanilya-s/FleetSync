package com.fleetsync.fleetsync.trip;

import jakarta.validation.constraints.*;

/**
 * TripRequest — represents the JSON body sent by the manager when assigning a driver to a vehicle for a trip.
 *
 * This is a DTO used to carry data from the HTTP request into TripController.
 * Spring deserializes the incoming JSON into this record automatically.
 *
 * Example JSON body:
 * {
 *   "driverId": 3,
 *   "vehicleId": 7,
 *   "origin": "Karachi Warehouse",
 *   "destination": "Lahore Distribution Center"
 * }
 */
public record TripRequest(

    /**
     * ID of the driver being assigned to this trip.
     * Must exist in the drivers table — validated in TripController.
     * @NotNull — must not be null (0 or missing is rejected)
     */
    @NotNull
    Long driverId,

    /**
     * ID of the vehicle being assigned to this trip.
     * Must exist in the vehicles table — validated in TripController.
     * @NotNull — must not be null
     */
    @NotNull
    Long vehicleId,

    /**
     * Starting location of the trip.
     * @NotBlank — must not be null or empty
     * @Size     — max 255 characters
     */
    @NotBlank
    @Size(max = 255)
    String origin,

    /**
     * Ending location of the trip.
     * @NotBlank — must not be null or empty
     * @Size     — max 255 characters
     */
    @NotBlank
    @Size(max = 255)
    String destination
) {}
