package com.fleetsync.fleetsync.vehicle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository repo;

    public VehicleController(VehicleRepository repo) {
        this.repo = repo;
    }

    // GET /api/vehicles — list all vehicles
    @GetMapping
    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    // POST /api/vehicles — add a new vehicle
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Valid @RequestBody VehicleRequest req) {
        if (repo.existsByPlateNumber(req.plateNumber()))
            throw new IllegalArgumentException("Plate number already registered");
        repo.save(req);
    }

    // DELETE /api/vehicles/{id} — remove a vehicle by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Vehicle not found");
        repo.deleteById(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
