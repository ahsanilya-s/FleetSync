package com.fleetsync.fleetsync.maintenance;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MaintenanceRequest — represents the JSON body sent by the manager when logging a maintenance record.
 *
 * This is a DTO used to carry data from the HTTP request into MaintenanceController.
 * Spring deserializes the incoming JSON into this record automatically.
 *
 * Validation annotations are checked by @Valid in MaintenanceController.
 * If any required field is missing or invalid, Spring returns 400 Bad Request.
 *
 * Example JSON body:
 * {
 *   "vehicleId": 3,
 *   "date": "2025-01-15",
 *   "type": "OIL_CHANGE",
 *   "description": "Changed engine oil and filter",
 *   "cost": 75.00,
 *   "nextServiceDate": "2025-07-15",
 *   "nextServiceMileage": 15000
 * }
 */
public record MaintenanceRequest(

    /**
     * ID of the vehicle this maintenance record belongs to.
     * Must exist in the vehicles table — validated in MaintenanceController.
     * @NotNull — must not be null
     */
    @NotNull
    Long vehicleId,

    /**
     * Date the maintenance was performed.
     * @NotNull — must not be null
     */
    @NotNull
    LocalDate date,

    /**
     * Type/category of maintenance (e.g. "OIL_CHANGE", "TIRE_ROTATION", "BRAKE_INSPECTION").
     * @NotBlank — must not be null or empty
     * @Size     — max 100 characters
     */
    @NotBlank
    @Size(max = 100)
    String type,

    /**
     * Optional free-text description of the work done.
     * Can be null or omitted in the request body.
     * @Size — max 500 characters if provided
     */
    @Size(max = 500)
    String description,

    /**
     * Cost of the maintenance in currency units.
     * @NotNull      — must not be null
     * @DecimalMin   — must be >= 0.0 (cost cannot be negative)
     * @Digits       — max 10 digits before decimal point, max 2 after (e.g. 9999999999.99)
     */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    BigDecimal cost,

    /**
     * Optional: scheduled date for the next service.
     * If provided, this record will appear in /upcoming and /alerts queries
     * when the date is approaching or has passed.
     * Can be null if no date-based schedule is needed.
     */
    LocalDate nextServiceDate,

    /**
     * Optional: odometer reading at which the next service is due.
     * If provided, this record will appear in /upcoming queries when
     * the currentMileage parameter meets or exceeds this value.
     * @Min(0) — mileage cannot be negative
     */
    @Min(0)
    Integer nextServiceMileage
) {}
