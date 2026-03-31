package com.fleetsync.fleetsync.maintenance;

import com.fleetsync.fleetsync.vehicle.VehicleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for vehicle maintenance management
// All endpoints are restricted to MANAGER role (enforced in SecurityConfig)
@Validated
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    // Default lookahead window for upcoming maintenance queries (days)
    private static final int DEFAULT_UPCOMING_DAYS = 30;

    // Default alert window: surface records due within this many days (or overdue)
    private static final int DEFAULT_ALERT_DAYS = 7;

    private final MaintenanceRepository maintenanceRepo;
    private final VehicleRepository vehicleRepo;

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
        if (!vehicleRepo.existsById(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle not found");
        maintenanceRepo.save(req);
    }

    // GET /api/maintenance/{vehicleId}
    // Returns the full maintenance history for a specific vehicle, newest first
    // Returns 404 Not Found if the vehicle does not exist
    @GetMapping("/{vehicleId}")
    public List<Maintenance> history(@PathVariable Long vehicleId) {
        if (!vehicleRepo.existsById(vehicleId))
            throw new IllegalArgumentException("Vehicle not found");
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    // GET /api/maintenance/upcoming
    // Returns maintenance records scheduled within the next [days] days (default: 30)
    // Optionally filters by currentMileage — includes records where next_service_mileage
    // is less than or equal to the provided mileage value
    // Query parameters:
    //   days          (optional, default 30) — lookahead window in days
    //   currentMileage (optional)            — current odometer reading to detect mileage-based alerts
    @GetMapping("/upcoming")
    public List<Maintenance> upcoming(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days,
            @RequestParam(required = false) Integer currentMileage) {
        return maintenanceRepo.findUpcoming(days, currentMileage);
    }

    // GET /api/maintenance/alerts
    // Returns maintenance records where the vehicle is overdue or due within [days] days (default: 7)
    // Fleet managers should poll this endpoint to surface service alerts
    // Query parameters:
    //   days (optional, default 7) — alert window in days
    @GetMapping("/alerts")
    public List<Maintenance> alerts(@RequestParam(defaultValue = "7") @Min(1) @Max(90) int days) {
        return maintenanceRepo.findAlerts(days);
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 404 if the message contains "not found", otherwise 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
