package com.fleetsync.fleetsync.maintenance;

import com.fleetsync.fleetsync.vehicle.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for maintenance record management
// All endpoints are restricted to MANAGER role (enforced in SecurityConfig)
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceRepository maintenanceRepo;
    private final VehicleRepository vehicleRepo; // Used to validate that the vehicle exists

    public MaintenanceController(MaintenanceRepository maintenanceRepo, VehicleRepository vehicleRepo) {
        this.maintenanceRepo = maintenanceRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // POST /api/maintenance
    // Logs a new maintenance record for a vehicle
    // Returns 201 Created on success
    // Returns 404 Not Found if the vehicle does not exist
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void log(@Valid @RequestBody MaintenanceRequest req) {
        // Ensure the vehicle exists before attaching a maintenance record
        if (!vehicleRepo.existsById(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle not found");
        maintenanceRepo.save(req);
    }

    // GET /api/maintenance/{vehicleId}
    // Returns all maintenance records for the specified vehicle ordered by date (newest first)
    // Returns 404 Not Found if the vehicle does not exist
    @GetMapping("/{vehicleId}")
    public List<Maintenance> getByVehicle(@PathVariable Long vehicleId) {
        // Ensure the vehicle exists before querying maintenance history
        if (!vehicleRepo.existsById(vehicleId))
            throw new IllegalArgumentException("Vehicle not found");
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 404 Not Found for missing vehicles
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
