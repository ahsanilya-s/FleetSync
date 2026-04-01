package com.fleetsync.fleetsync.driver;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DriverController — REST controller for fleet driver management.
 *
 * Exposes endpoints under /api/drivers.
 * All endpoints in this controller are restricted to the MANAGER role (enforced in SecurityConfig).
 *
 * @RestController — marks this as a REST controller; all methods return JSON automatically.
 * @RequestMapping — sets the base URL path for all methods in this class.
 */
@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    /**
     * DriverRepository handles all database operations for drivers.
     * Injected by Spring via constructor injection.
     */
    private final DriverRepository repo;

    public DriverController(DriverRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/drivers
     * Returns all registered drivers with their current assignment status.
     *
     * The status field in each Driver will be either "AVAILABLE" or "ON_TRIP".
     * This lets the manager see which drivers are free to be assigned to new trips.
     *
     * @return a JSON array of Driver objects, newest first
     */
    @GetMapping
    public List<Driver> getAll() {
        return repo.findAll();
    }

    /**
     * POST /api/drivers
     * Registers a new driver in the fleet system.
     *
     * @Valid         — triggers validation on DriverRequest fields before this method runs.
     * @RequestBody   — Spring deserializes the incoming JSON body into a DriverRequest object.
     * @ResponseStatus(CREATED) — returns HTTP 201 on success.
     *
     * Returns 409 Conflict if the license number or email is already registered.
     *
     * @param req the driver data from the request body
     * @throws IllegalArgumentException if license number or email is already in use
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody DriverRequest req) {
        // Prevent two drivers from having the same license number
        if (repo.existsByLicense(req.licenseNumber()))
            throw new IllegalArgumentException("License number already registered");

        // Prevent two drivers from sharing the same email address
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already registered");

        repo.save(req);
    }

    /**
     * Exception handler for IllegalArgumentException thrown by any method in this controller.
     *
     * Returns HTTP 409 Conflict — appropriate for duplicate data violations
     * (e.g. license number or email already exists).
     *
     * ProblemDetail produces JSON like: { "status": 409, "detail": "License number already registered" }
     *
     * @param ex the thrown exception
     * @return a ProblemDetail with status 409 and the exception message as detail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
