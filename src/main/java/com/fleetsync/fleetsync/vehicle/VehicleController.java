package com.fleetsync.fleetsync.vehicle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VehicleController — REST controller for fleet vehicle management.
 *
 * Exposes CRUD endpoints under /api/vehicles.
 * All endpoints in this controller are restricted to the MANAGER role (enforced in SecurityConfig).
 *
 * @RestController — marks this as a REST controller; all methods return JSON automatically.
 * @RequestMapping — sets the base URL path for all methods in this class.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    /**
     * VehicleRepository handles all database operations for vehicles.
     * Injected by Spring via constructor injection.
     */
    private final VehicleRepository repo;

    public VehicleController(VehicleRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/vehicles
     * Returns a list of all vehicles in the fleet, ordered by creation date (newest first).
     *
     * @return a JSON array of Vehicle objects
     */
    @GetMapping
    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    /**
     * POST /api/vehicles
     * Adds a new vehicle to the fleet.
     *
     * @Valid         — triggers validation on VehicleRequest fields before this method runs.
     *                  If validation fails, Spring throws MethodArgumentNotValidException (→ 400).
     * @RequestBody   — Spring deserializes the incoming JSON body into a VehicleRequest object.
     * @ResponseStatus(CREATED) — returns HTTP 201 on success (correct REST convention for creation).
     *
     * Returns 409 Conflict if the plate number is already registered to another vehicle.
     *
     * @param req the vehicle data from the request body
     * @throws IllegalArgumentException if the plate number is already in use
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Valid @RequestBody VehicleRequest req) {
        // Check for duplicate plate number before inserting
        if (repo.existsByPlateNumber(req.plateNumber()))
            throw new IllegalArgumentException("Plate number already registered");
        repo.save(req);
    }

    /**
     * PUT /api/vehicles/{id}
     * Updates the details of an existing vehicle identified by its ID.
     *
     * @PathVariable — extracts the {id} segment from the URL and binds it to the "id" parameter.
     *                 e.g. PUT /api/vehicles/5 → id = 5
     *
     * Returns 204 No Content on success (no body needed — the update was applied).
     * Returns 404 Not Found if no vehicle exists with the given ID.
     * Returns 409 Conflict if the new plate number belongs to a different vehicle.
     *
     * @param id  the ID of the vehicle to update
     * @param req the updated vehicle data from the request body
     * @throws IllegalArgumentException if the vehicle is not found or plate is taken
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        // Verify the vehicle exists before attempting an update
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Vehicle not found");

        // Allow the same plate for the same vehicle, but reject if it belongs to a different one
        // existsByPlateNumber(plate, excludeId) excludes the current vehicle from the duplicate check
        if (repo.existsByPlateNumber(req.plateNumber(), id))
            throw new IllegalArgumentException("Plate number already registered");

        repo.update(id, req);
    }

    /**
     * DELETE /api/vehicles/{id}
     * Permanently removes a vehicle from the fleet.
     *
     * Returns 204 No Content on success.
     * Returns 404 Not Found if no vehicle exists with the given ID.
     *
     * @param id the ID of the vehicle to delete
     * @throws IllegalArgumentException if the vehicle is not found
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // Verify the vehicle exists before attempting deletion
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Vehicle not found");
        repo.deleteById(id);
    }

    /**
     * Exception handler for IllegalArgumentException thrown by any method in this controller.
     *
     * Determines the HTTP status based on the error message:
     *   - "not found" in the message → 404 Not Found (resource doesn't exist)
     *   - anything else              → 409 Conflict (e.g. duplicate plate number)
     *
     * ProblemDetail is a standardized RFC 7807 error response format.
     * It produces JSON like: { "status": 404, "detail": "Vehicle not found" }
     *
     * @param ex the thrown exception
     * @return a ProblemDetail with the appropriate HTTP status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        // Choose 404 for "not found" errors, 409 for conflicts (e.g. duplicate plate)
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getMessage());  // attach the error message to the response body
        return problem;
    }
}
