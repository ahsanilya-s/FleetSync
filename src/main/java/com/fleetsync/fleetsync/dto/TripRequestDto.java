package com.fleetsync.fleetsync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripRequestDto {
//purpose of this is origing
    @NotBlank(message = "Origin can not be Blank")
    private String origin;

    @NotBlank(message = "Destination can not be Blank")
    private String destination;
//test comment for vehicle
    @NotNull(message = "Vehicle Id is required")
    private Long vehicleId;

    @NotNull(message = "Driver Id is required")
    private Long driverId;

    private String notes;
}
