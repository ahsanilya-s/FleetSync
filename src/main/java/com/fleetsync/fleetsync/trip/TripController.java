package com.fleetsync.fleetsync.trip;

import com.fleetsync.fleetsync.driver.DriverRepository;
import com.fleetsync.fleetsync.vehicle.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// REST controller for trip management
// POST /api/trips and GET /api/trips are restricted to MANAGER role
// PUT /api/trips/{id}/status is accessible by both MANAGER and DRIVER (see SecurityConfig)
@RestController
@RequestMapping("/api/trips")
public class TripController {

    // Valid terminal statuses — driver and vehicle are freed when the trip reaches one of these
    private static final Set<String> TERMINAL_STATUSES = Set.of("COMPLETED", "CANCELLED");

    private final TripRepository tripRepo;
    private final DriverRepository driverRepo;   // Used to validate driver exists and update their status
    private final VehicleRepository vehicleRepo; // Used to validate vehicle exists and update its status

    public TripController(TripRepository tripRepo, DriverRepository driverRepo, VehicleRepository vehicleRepo) {
        this.tripRepo = tripRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // GET /api/trips
    // Returns all trips, optionally filtered by driverId or vehicleId query parameter
    // Supports trip history for a specific driver or vehicle
    @GetMapping
    public List<Trip> getAll(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long vehicleId) {
        if (driverId != null) return tripRepo.findAllByDriverId(driverId);
        if (vehicleId != null) return tripRepo.findAllByVehicleId(vehicleId);
        return tripRepo.findAll();
    }

    // POST /api/trips
    // Creates a new trip by assigning a driver to a vehicle
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

        // Update driver and vehicle statuses to ON_TRIP to reflect their reservation
        driverRepo.updateStatus(req.driverId(), "ON_TRIP");
        vehicleRepo.updateStatus(req.vehicleId(), "ON_TRIP");
    }

    // PUT /api/trips/{id}/status
    // Updates the status of a trip following the lifecycle: SCHEDULED → IN_PROGRESS → COMPLETED / CANCELLED
    // Accessible by both MANAGER and DRIVER roles (see SecurityConfig)
    // Sets start_time when trip moves to IN_PROGRESS, end_time when COMPLETED or CANCELLED
    // Resets driver and vehicle statuses to AVAILABLE when the trip ends
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @Valid @RequestBody TripStatusRequest req) {
        Trip trip = tripRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        String current = trip.status();
        String next = req.status().toUpperCase();

        // Validate the requested status is a known value
        if (!Set.of("IN_PROGRESS", "COMPLETED", "CANCELLED").contains(next))
            throw new IllegalArgumentException("Invalid status: must be IN_PROGRESS, COMPLETED, or CANCELLED");

        // Enforce valid status transitions
        boolean valid = switch (current) {
            case "SCHEDULED"   -> next.equals("IN_PROGRESS") || next.equals("CANCELLED");
            case "IN_PROGRESS" -> next.equals("COMPLETED")   || next.equals("CANCELLED");
            default            -> false; // COMPLETED and CANCELLED are terminal — no further transitions
        };

        if (!valid)
            throw new IllegalArgumentException(
                "Cannot transition trip from " + current + " to " + next);

        // Set start_time when the trip moves to IN_PROGRESS
        LocalDateTime startTime = next.equals("IN_PROGRESS") ? LocalDateTime.now() : null;
        // Set end_time when the trip reaches a terminal state
        LocalDateTime endTime   = TERMINAL_STATUSES.contains(next) ? LocalDateTime.now() : null;

        tripRepo.updateStatus(id, next, startTime, endTime);

        // When the trip ends, free up the driver and vehicle
        if (TERMINAL_STATUSES.contains(next)) {
            driverRepo.updateStatus(trip.driverId(), "AVAILABLE");
            vehicleRepo.updateStatus(trip.vehicleId(), "AVAILABLE");
        }
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 404 for missing resources, 409 for conflicts, 400 for invalid input
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status;
        String msg = ex.getMessage();
        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.contains("already on an active trip")) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(msg);
        return problem;
    }
}
