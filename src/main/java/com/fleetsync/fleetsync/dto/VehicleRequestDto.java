package com.fleetsync.fleetsync.dto;
import com.fleetsync.fleetsync.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class VehicleRequestDto {
    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Capacity is Required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

}
