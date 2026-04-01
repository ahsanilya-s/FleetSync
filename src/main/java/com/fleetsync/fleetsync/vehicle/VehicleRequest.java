package com.fleetsync.fleetsync.vehicle;

import jakarta.validation.constraints.*;

/**
 * VehicleRequest — represents the JSON body sent by the manager when adding or updating a vehicle.
 *
 * This is a DTO (Data Transfer Object) — it carries data from the HTTP request into the controller.
 * Spring deserializes the incoming JSON into this record automatically.
 *
 * Validation annotations are checked by @Valid in VehicleController.
 * If any field fails, Spring returns 400 Bad Request before the controller method runs.
 *
 * Example JSON body:
 * {
 *   "name": "Truck Alpha",
 *   "type": "Box Truck",
 *   "plateNumber": "ABC-1234",
 *   "capacity": 5000,
 *   "fuelType": "Diesel"
 * }
 */
public record VehicleRequest(

    /**
     * Display name of the vehicle.
     * @NotBlank — must not be null or empty
     * @Size     — max 100 characters
     */
    @NotBlank
    @Size(max = 100)
    String name,

    /**
     * Vehicle category (e.g. "Mini Van", "Cargo Van", "Pickup Truck", "Box Truck").
     * @NotBlank — must not be null or empty
     * @Size     — max 50 characters
     */
    @NotBlank
    @Size(max = 50)
    String type,

    /**
     * Unique license plate number.
     * Uniqueness is enforced in VehicleController (not just at DB level) to return a clear 409 error.
     * @NotBlank — must not be null or empty
     * @Size     — max 20 characters
     */
    @NotBlank
    @Size(max = 20)
    String plateNumber,

    /**
     * Load or passenger capacity of the vehicle.
     * @Min(1) — must be at least 1 (a vehicle with 0 capacity makes no sense)
     */
    @Min(1)
    int capacity,

    /**
     * Fuel type (e.g. "Petrol", "Diesel", "Electric", "Hybrid").
     * @NotBlank — must not be null or empty
     * @Size     — max 30 characters
     */
    @NotBlank
    @Size(max = 30)
    String fuelType
) {}
