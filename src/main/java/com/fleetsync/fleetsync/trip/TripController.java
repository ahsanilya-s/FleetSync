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

/**
 * TripController — REST controller for trip assignment and lifecycle management.
 *
 * Exposes endpoints under /api/trips.
 * Access rules (from SecurityConfig):
 *   - POST /api/trips              → MANAGER only
 *   - GET  /api/trips              → MANAGER and DRIVER
 *   - PUT  /api/trips/{id}/status  → MANAGER and DRIVER
 */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    /**
     * Terminal statuses — once a trip reaches one of these, no further transitions are allowed.
     * When a trip reaches a terminal status, the driver and vehicle are freed (set to AVAILABLE).
     *
     * Set.of() creates an immutable set — good for membership checks (contains()).
     */
    private static final Set<String> TERMINAL_STATUSES = Set.of("COMPLETED", "CANCELLED");

    private final TripRepository tripRepo;
    private final DriverRepository driverRepo;    // needed to validate driver exists and update their status
    private final VehicleRepository vehicleRepo;  // needed to validate vehicle exists and update its status

    public TripController(TripRepository tripRepo, DriverRepository driverRepo, VehicleRepository vehicleRepo) {
        this.tripRepo = tripRepo;
        this.driverRepo = driverRepo;
        this.vehicleRepo = vehicleRepo;
    }

    /**
     * GET /api/trips
     * Returns all trips, optionally filtered by driverId or vehicleId.
     *
     * @RequestParam(required = false) — these query parameters are optional.
     * If provided, only trips for that driver/vehicle are returned.
     * If neither is provided, all trips are returned.
     *
     * Example URLs:
     *   GET /api/trips                    → all trips
     *   GET /api/trips?driverId=3         → trips for driver with ID 3
     *   GET /api/trips?vehicleId=7        → trips for vehicle with ID 7
     *
     * @param driverId  optional filter by driver ID
     * @param vehicleId optional filter by vehicle ID
     * @return a JSON array of Trip objects
     */
    @GetMapping
    public List<Trip> getAll(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long vehicleId) {
        if (driverId != null) return tripRepo.findAllByDriverId(driverId);
        if (vehicleId != null) return tripRepo.findAllByVehicleId(vehicleId);
        return tripRepo.findAll();
    }

    /**
     * POST /api/trips
     * Creates a new trip by assigning a driver to a vehicle for a journey.
     *
     * Validation steps (in order):
     *   1. Driver must exist in the database
     *   2. Vehicle must exist in the database
     *   3. Driver must not already be on an active trip (SCHEDULED or IN_PROGRESS)
     *   4. Vehicle must not already be on an active trip
     *
     * On success:
     *   - Trip is saved with status "SCHEDULED"
     *   - Driver status is updated to "ON_TRIP"
     *   - Vehicle status is updated to "ON_TRIP"
     *
     * @param req the trip assignment data from the request body
     * @throws IllegalArgumentException if any validation step fails
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void assign(@Valid @RequestBody TripRequest req) {
        // Step 1: Ensure the driver exists before assigning
        if (!driverRepo.existsById(req.driverId()))
            throw new IllegalArgumentException("Driver not found");

        // Step 2: Ensure the vehicle exists before assigning
        if (!vehicleRepo.existsById(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle not found");

        // Step 3: Prevent assigning a driver who is already on another active trip
        if (tripRepo.driverHasActiveTrip(req.driverId()))
            throw new IllegalArgumentException("Driver is already on an active trip");

        // Step 4: Prevent assigning a vehicle that is already on another active trip
        if (tripRepo.vehicleHasActiveTrip(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle is already on an active trip");

        // Save the trip (status defaults to SCHEDULED in the DB)
        tripRepo.save(req);

        // Mark both the driver and vehicle as ON_TRIP so they can't be double-booked
        driverRepo.updateStatus(req.driverId(), "ON_TRIP");
        vehicleRepo.updateStatus(req.vehicleId(), "ON_TRIP");
    }

    /**
     * PUT /api/trips/{id}/status
     * Updates the status of a trip following its lifecycle.
     *
     * Valid transitions:
     *   SCHEDULED   → IN_PROGRESS  (driver starts the trip)
     *   SCHEDULED   → CANCELLED    (trip cancelled before starting)
     *   IN_PROGRESS → COMPLETED    (trip finishes successfully)
     *   IN_PROGRESS → CANCELLED    (trip cancelled mid-way)
     *
     * Side effects on terminal transitions (COMPLETED or CANCELLED):
     *   - Driver status is reset to "AVAILABLE"
     *   - Vehicle status is reset to "AVAILABLE"
     *
     * Timestamps are set automatically:
     *   - start_time is set when transitioning to IN_PROGRESS
     *   - end_time is set when transitioning to COMPLETED or CANCELLED
     *
     * @param id  the ID of the trip to update (from the URL path)
     * @param req the new status from the request body
     * @throws IllegalArgumentException if the trip is not found or the transition is invalid
     */
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @Valid @RequestBody TripStatusRequest req) {
        // Load the current trip — throws 404 if not found
        Trip trip = tripRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        String current = trip.status();                    // e.g. "SCHEDULED"
        String next    = req.status().toUpperCase();       // normalize to uppercase

        // Reject unknown status values before checking transitions
        if (!Set.of("IN_PROGRESS", "COMPLETED", "CANCELLED").contains(next))
            throw new IllegalArgumentException("Invalid status: must be IN_PROGRESS, COMPLETED, or CANCELLED");

        // Use a switch expression to determine if the requested transition is valid
        // switch expressions (Java 14+) return a value — cleaner than if/else chains
        boolean valid = switch (current) {
            case "SCHEDULED"   -> next.equals("IN_PROGRESS") || next.equals("CANCELLED");
            case "IN_PROGRESS" -> next.equals("COMPLETED")   || next.equals("CANCELLED");
            default            -> false;  // COMPLETED and CANCELLED are terminal — no further transitions
        };

        if (!valid)
            throw new IllegalArgumentException(
                "Cannot transition trip from " + current + " to " + next);

        // Set start_time only when moving to IN_PROGRESS, null otherwise (COALESCE keeps existing value)
        LocalDateTime startTime = next.equals("IN_PROGRESS") ? LocalDateTime.now() : null;

        // Set end_time only when reaching a terminal status, null otherwise
        LocalDateTime endTime   = TERMINAL_STATUSES.contains(next) ? LocalDateTime.now() : null;

        tripRepo.updateStatus(id, next, startTime, endTime);

        // When the trip ends, free up the driver and vehicle for new assignments
        if (TERMINAL_STATUSES.contains(next)) {
            driverRepo.updateStatus(trip.driverId(), "AVAILABLE");
            vehicleRepo.updateStatus(trip.vehicleId(), "AVAILABLE");
        }
    }

    /**
     * Exception handler for IllegalArgumentException thrown by any method in this controller.
     *
     * Determines the HTTP status based on the error message content:
     *   - "not found"              → 404 Not Found
     *   - "already on an active trip" → 409 Conflict
     *   - anything else            → 400 Bad Request (e.g. invalid status transition)
     *
     * @param ex the thrown exception
     * @return a ProblemDetail with the appropriate HTTP status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status;
        String msg = ex.getMessage();
        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.contains("already on an active trip")) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;  // invalid status transition or unknown status value
        }
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(msg);
        return problem;
    }
}
