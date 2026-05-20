package com.fleetsync.fleetsync.controller;


import com.fleetsync.fleetsync.dto.VehicleRequestDto;
import com.fleetsync.fleetsync.dto.VehicleResponseDto;
import com.fleetsync.fleetsync.entity.Vehicle;
import com.fleetsync.fleetsync.enums.VehicleStatus;
import com.fleetsync.fleetsync.exception.DuplicateResourceException;
import com.fleetsync.fleetsync.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class VehicleController {
        private final VehicleRepository vehicleRepository;

        @Transactional
        public VehicleResponseDto createVehicle(VehicleRequestDto req) {
                if(vehicleRepository.existsByPlateNumber(req.getPlateNumber())) throw new DuplicateResourceException("Vehicle with plate number " + req.getPlateNumber() + " already exists");

                Vehicle vehicle = Vehicle.builder().plateNumber(req.getPlateNumber().toUpperCase())
                        .type(req.getType())
                        .model(req.getModel())
                        .capacity(req.getCapacity())
                        .status(VehicleStatus.AVAILABLE).build();
                return toDto(vehicleRepository.save(vehicle));
        }



    private VehicleResponseDto toDto(Vehicle vehicle){
                return VehicleResponseDto.builder()
                        .id(vehicle.getId())
                        .plateNumber(vehicle.getPlateNumber())
                        .type(vehicle.getType())
                        .model(vehicle.getModel())
                        .capacity(vehicle.getCapacity())
                        .status(vehicle.getStatus())
                        .build();
        }
}
