package com.fleetsync.fleetsync.dto;

import com.fleetsync.fleetsync.enums.VehicleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponseDto {
    private Long id;
    private String plateNumber;
    private String model;
    private Integer capacity;
    private VehicleStatus status;
}
