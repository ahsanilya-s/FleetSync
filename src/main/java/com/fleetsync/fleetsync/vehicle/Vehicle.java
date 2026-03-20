package com.fleetsync.fleetsync.vehicle;

import java.time.LocalDateTime;

public record Vehicle(
    Long id,
    String name,
    String type,
    String plateNumber,
    int capacity,
    String status,
    LocalDateTime createdAt
) {}
