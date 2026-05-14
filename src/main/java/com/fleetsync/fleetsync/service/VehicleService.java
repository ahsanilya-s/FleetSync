package com.fleetsync.fleetsync.service;

import com.fleetsync.fleetsync.dto.VehicleRequestDto;
import com.fleetsync.fleetsync.dto.VehicleResponseDto;
import com.fleetsync.fleetsync.entity.Vehicle;
import com.fleetsync.fleetsync.enums.VehicleStatus;
import com.fleetsync.fleetsync.enums.VehicleType;
import com.fleetsync.fleetsync.exception.BusinessException;
import com.fleetsync.fleetsync.exception.DuplicateResourceException;
import com.fleetsync.fleetsync.exception.ResourceNotFoundException;
import com.fleetsync.fleetsync.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponseDto createVehicle(VehicleRequestDto req) {
        if (vehicleRepository.existsByPlateNumber(req.getPlateNumber())) {
            throw new DuplicateResourceException("Vehicle with plate " + req.getPlateNumber() + " already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(req.getPlateNumber().toUpperCase())
                .type(req.getType())
                .model(req.getModel())
                .capacity(req.getCapacity())
                .status(VehicleStatus.AVAILABLE)
                .build();

        return toDto(vehicleRepository.save(vehicle));
    }

    public List<VehicleResponseDto> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<VehicleResponseDto> findById(Long id){
        return vehicleRepository.findById(id).stream().map(this::toDto).toList();
    }

    public List<VehicleResponseDto> getByType(VehicleType type){
        return vehicleRepository.findByType(type).stream().map(this::toDto).toList();
    }

    @Transactional
    public VehicleResponseDto updateVehicle(Long id, VehicleRequestDto req){
        Vehicle vehicle = findOrThrow(id);
        String newPlateNumber = req.getPlateNumber().toUpperCase();
        if(vehicleRepository.existsByPlateNumber(newPlateNumber) && !vehicle.getPlateNumber().equals(newPlateNumber)){
            throw new DuplicateResourceException("Vehicle with plate " + newPlateNumber + " already exists");
        }
        vehicle.setPlateNumber(newPlateNumber);
        vehicle.setModel(req.getModel());
        vehicle.setCapacity(req.getCapacity());
        vehicle.setType(req.getType());
        return toDto(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long id){
        Vehicle vehicle = findOrThrow(id);
        if(vehicle.getStatus()==VehicleStatus.ON_TRIP){
            throw new BusinessException("Vehicle is on a trip");
        }

        vehicleRepository.delete(vehicle);
    }

    private Vehicle findOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }



    private VehicleResponseDto toDto(@NonNull Vehicle v) {
        return VehicleResponseDto.builder()
                .id(v.getId())
                .plateNumber(v.getPlateNumber())
                .model(v.getModel())
                .capacity(v.getCapacity())
                .status(v.getStatus())
                .build();
    }
}
