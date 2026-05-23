package com.fleetsync.fleetsync.controller;

import com.fleetsync.fleetsync.dto.VehicleRequestDto;
import com.fleetsync.fleetsync.dto.VehicleResponseDto;
import com.fleetsync.fleetsync.enums.VehicleType;
import com.fleetsync.fleetsync.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDto> createVehicle(@Valid @RequestBody VehicleRequestDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(req));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByType(@PathVariable String type) {
        return ResponseEntity.ok(vehicleService.getByType(VehicleType.valueOf(type.toUpperCase())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequestDto req) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
