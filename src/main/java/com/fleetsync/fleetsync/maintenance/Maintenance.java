package com.fleetsync.fleetsync.maintenance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Represents a maintenance record as returned from the database
// Each field maps directly to a column in the maintenance table
public record Maintenance(
    Long id,
    Long vehicleId,        // FK → vehicles.id
    LocalDate date,        // Date the maintenance was performed
    String type,           // e.g. OIL_CHANGE, TIRE_ROTATION, INSPECTION
    String description,
    BigDecimal cost,
    LocalDateTime createdAt
) {}
