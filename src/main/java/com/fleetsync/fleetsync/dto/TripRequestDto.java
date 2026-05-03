package com.fleetsync.fleetsync.dto;

import com.fleetsync.fleetsync.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripRequestDto {
    @NotBlank(message = "Origin can not be Blank")
    private String origin;
    @NotBlank(message = "Destination can not be Blank")
    private String destination;
    @NotNull(message = "Vehicle Id can not be Blank")
    private Long vehicleId;
    @NotNull(message = "Driver Id can not be Blank")
    private Long driverId;
    @NotNull(message = "Trip Status must be active")
    private TripStatus status;
    @NotNull(message = "Estimated Fare can not be Blank")
    private Double estimatedFare;

}
