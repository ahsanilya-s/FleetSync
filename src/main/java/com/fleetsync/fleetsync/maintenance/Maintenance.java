package com.fleetsync.fleetsync.maintenance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Represents a maintenance record as returned from the database
// Each field maps directly to a column in the maintenance table
public record Maintenance(
    Long id,
    Long vehicleId,
    LocalDate date,            // Date the maintenance was performed
    String type,               // e.g. OIL_CHANGE, TIRE_ROTATION, BRAKE_INSPECTION
    String description,        // Optional details about the maintenance work
    BigDecimal cost,
    LocalDate nextServiceDate,    // Scheduled date for next service (schedule-based)
    Integer nextServiceMileage,   // Mileage at which next service is due (mileage-based)
    LocalDateTime createdAt
) {}
