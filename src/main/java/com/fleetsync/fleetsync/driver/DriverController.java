package com.fleetsync.fleetsync.driver;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for driver management
// All endpoints are restricted to MANAGER role (enforced in SecurityConfig)
@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository repo;

    public DriverController(DriverRepository repo) {
        this.repo = repo;
    }

    // GET /api/drivers
    // Returns all registered drivers with their current assignment status
    // Status will be AVAILABLE or ON_TRIP
    @GetMapping
    public List<Driver> getAll() {
        return repo.findAll();
    }

    // POST /api/drivers
    // Registers a new driver with their license and contact information
    // Returns 201 Created on success
    // Returns 409 Conflict if license number or email is already registered
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody DriverRequest req) {
        // Prevent duplicate license numbers across the fleet
        if (repo.existsByLicense(req.licenseNumber()))
            throw new IllegalArgumentException("License number already registered");

        // Prevent duplicate email addresses
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already registered");

        repo.save(req);
    }

    // Handles IllegalArgumentException thrown by any method in this controller
    // Returns 409 Conflict for duplicate data violations
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
