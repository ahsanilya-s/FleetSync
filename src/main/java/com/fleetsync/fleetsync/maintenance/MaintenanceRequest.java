package com.fleetsync.fleetsync.maintenance;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Represents the JSON body sent by the manager when logging a maintenance record
// Validation annotations ensure all required fields are present and correctly formatted
public record MaintenanceRequest(

    // ID of the vehicle this maintenance record belongs to
    @NotNull
    Long vehicleId,

    // Date the maintenance was performed
    @NotNull
    LocalDate date,

    // Type of maintenance (e.g. OIL_CHANGE, TIRE_ROTATION, BRAKE_INSPECTION)
    @NotBlank
    @Size(max = 100)
    String type,

    // Optional description of the work done
    @Size(max = 500)
    String description,

    // Cost of the maintenance in currency units
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    BigDecimal cost,

    // Optional: scheduled date for next service (schedule-based upcoming maintenance)
    LocalDate nextServiceDate,

    // Optional: mileage at which next service is due (mileage-based upcoming maintenance)
    @Min(0)
    Integer nextServiceMileage
) {}
