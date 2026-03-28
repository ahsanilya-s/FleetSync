package com.fleetsync.fleetsync.driver;

import java.time.LocalDateTime;

// Represents a driver record as returned from the database
// Each field maps directly to a column in the drivers table
public record Driver(
    Long id,
    String fullName,
    String licenseNumber,  // Unique driving license ID
    String phone,
    String email,
    String status,         // AVAILABLE or ON_TRIP
    LocalDateTime createdAt
) {}
