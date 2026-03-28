package com.fleetsync.fleetsync.maintenance;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Represents the JSON body sent by the manager when logging a maintenance record
public record MaintenanceRequest(

    // ID of the vehicle that received maintenance — must exist in the vehicles table
    @NotNull
    Long vehicleId,

    // Date the maintenance was performed
    @NotNull
    LocalDate date,

    // Type of maintenance performed (e.g. OIL_CHANGE, TIRE_ROTATION, INSPECTION)
    @NotBlank
    @Size(max = 100)
    String type,

    // Optional description of the work done
    @Size(max = 1000)
    String description,

    // Cost of the maintenance in the local currency
    @NotNull
    @DecimalMin("0.00")
    BigDecimal cost
) {}
