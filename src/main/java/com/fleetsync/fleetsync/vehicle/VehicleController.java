package com.fleetsync.fleetsync.vehicle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Marks this class as a REST controller — all methods return JSON responses
// Base path for all vehicle endpoints is /api/vehicles
// Access to all endpoints in this controller is restricted to MANAGER role (enforced in SecurityConfig)
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    // Repository handles all database operations for vehicles
    private final VehicleRepository repo;

    // Spring injects VehicleRepository automatically via constructor injection
    public VehicleController(VehicleRepository repo) {
        this.repo = repo;
    }

    // GET /api/vehicles
    // Returns a list of all vehicles ordered by creation date (newest first)
    // Only accessible to authenticated users with MANAGER role
    @GetMapping
    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    // POST /api/vehicles
    // Adds a new vehicle to the fleet
    // @Valid triggers validation on VehicleRequest fields (e.g. @NotBlank, @Min)
    // Returns 201 Created on success
    // Returns 409 Conflict if the plate number is already registered
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Valid @RequestBody VehicleRequest req) {
        // Prevent duplicate plate numbers across the fleet
        if (repo.existsByPlateNumber(req.plateNumber()))
            throw new IllegalArgumentException("Plate number already registered");
        repo.save(req);
    }

    // PUT /api/vehicles/{id}
    // Updates details of an existing vehicle identified by its ID
    // @Valid triggers validation on the incoming request body
    // Returns 204 No Content on success
    // Returns 404 Not Found if no vehicle exists with the given ID
    // Returns 409 Conflict if the new plate number belongs to a different vehicle
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        // Ensure the vehicle being updated actually exists
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Vehicle not found");
        // Allow the same plate number for the same vehicle, but reject if it belongs to another
        if (repo.existsByPlateNumber(req.plateNumber(), id))
            throw new IllegalArgumentException("Plate number already registered");
        repo.update(id, req);
    }

    // DELETE /api/vehicles/{id}
    // Removes a vehicle from the fleet permanently
    // Returns 204 No Content on success
    // Returns 404 Not Found if no vehicle exists with the given ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // Verify the vehicle exists before attempting deletion
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Vehicle not found");
        repo.deleteById(id);
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 404 if the message contains "not found", otherwise 409 Conflict
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        // Attach the exception message as the error detail in the response body
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
