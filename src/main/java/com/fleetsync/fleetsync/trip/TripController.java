package com.fleetsync.fleetsync.trip;

import com.fleetsync.fleetsync.driver.DriverRepository;
import com.fleetsync.fleetsync.vehicle.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for trip assignment management
// All endpoints are restricted to MANAGER role (enforced in SecurityConfig)
@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepo;
    private final DriverRepository driverRepo;   // Used to validate driver exists and update their status
    private final VehicleRepository vehicleRepo; // Used to validate vehicle exists

    public TripController(TripRepository tripRepo, DriverRepository driverRepo, VehicleRepository vehicleRepo) {
        this.tripRepo = tripRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // GET /api/trips
    // Returns all trip assignments ordered by creation date (newest first)
    @GetMapping
    public List<Trip> getAll() {
        return tripRepo.findAll();
    }

    // POST /api/trips
    // Assigns a driver to a vehicle for a specific trip
    // Returns 201 Created on success
    // Returns 404 Not Found if driver or vehicle does not exist
    // Returns 409 Conflict if driver or vehicle is already on an active trip
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void assign(@Valid @RequestBody TripRequest req) {
        // Ensure the driver exists in the database before assigning
        if (!driverRepo.existsById(req.driverId()))
            throw new IllegalArgumentException("Driver not found");

        // Ensure the vehicle exists in the database before assigning
        if (!vehicleRepo.existsById(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle not found");

        // A driver cannot be assigned to two trips at the same time
        if (tripRepo.driverHasActiveTrip(req.driverId()))
            throw new IllegalArgumentException("Driver is already on an active trip");

        // A vehicle cannot be used in two trips at the same time
        if (tripRepo.vehicleHasActiveTrip(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle is already on an active trip");

        // Save the trip assignment to the database
        tripRepo.save(req);

        // Update the driver's status to ON_TRIP so it reflects in GET /api/drivers
        driverRepo.updateStatus(req.driverId(), "ON_TRIP");
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 404 for missing resources, 409 for conflicts
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
