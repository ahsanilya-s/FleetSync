package com.fleetsync.fleetsync.vehicle;

import jakarta.validation.constraints.*;

public record VehicleRequest(

    @NotBlank
    @Size(max = 100)
    String name,

    // e.g. "Mini Van", "Cargo Van", "Pickup Truck", "Box Truck"
    @NotBlank
    @Size(max = 50)
    String type,

    @NotBlank
    @Size(max = 20)
    String plateNumber,

    @Min(1)
    int capacity
) {}
