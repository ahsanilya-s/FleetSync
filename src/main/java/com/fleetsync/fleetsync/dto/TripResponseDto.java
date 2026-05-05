package com.fleetsync.fleetsync.dto;

import com.fleetsync.fleetsync.enums.TripStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TripResponseDto {
    private Long id;
    private String origin;
    private String destination;
    private TripStatus status;
    private String notes;
    private Long driverId;
    private Long vehicleId;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
