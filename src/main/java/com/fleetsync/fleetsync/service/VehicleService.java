package com.fleetsync.fleetsync.service;

import com.fleetsync.fleetsync.dto.VehicleRequestDto;
import com.fleetsync.fleetsync.dto.VehicleResponseDto;
import com.fleetsync.fleetsync.entity.Vehicle;
import com.fleetsync.fleetsync.enums.VehicleStatus;
import com.fleetsync.fleetsync.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponseDto createVehicle(VehicleRequestDto req) {
        if(vehicleRepository.existsByPlateNumber(req.getPlateNumber())) {
            throw new IllegalArgumentException("Vehicle with this plate number already exists");
        }
        
        Vehicle vehicle = Vehicle.builder()
                .plateNumber(req.getPlateNumber())
                .type(req.getType())
                .model(req.getModel())
                .capacity(req.getCapacity())
                .status(VehicleStatus.AVAILABLE)
                .build();
        return  VehicleResponseDto.builder()
                .id(vehicleRepository.save(vehicle).getId())
                .plateNumber(vehicle.getPlateNumber())
                .model(vehicle.getModel())
                .capacity(vehicle.getCapacity())
                .status(vehicle.getStatus())
                .build();
    }
}
