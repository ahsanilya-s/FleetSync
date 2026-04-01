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

/**
 * MaintenanceController — REST controller for vehicle maintenance management.
 *
 * Exposes endpoints under /api/maintenance.
 * All endpoints are restricted to the MANAGER role (enforced in SecurityConfig).
 *
 * @Validated — enables validation of @RequestParam values (like @Min, @Max on query parameters).
 *              Without this, @Min/@Max on method parameters would be silently ignored.
 *              @Valid only works on @RequestBody — @Validated is needed for method-level params.
 */
@Validated
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    /**
     * Default lookahead window for the /upcoming endpoint.
     * Returns maintenance due within the next 30 days if no "days" param is provided.
     */
    private static final int DEFAULT_UPCOMING_DAYS = 30;

    /**
     * Default alert window for the /alerts endpoint.
     * Returns maintenance due within the next 7 days (or overdue) if no "days" param is provided.
     */
    private static final int DEFAULT_ALERT_DAYS = 7;

    private final MaintenanceRepository maintenanceRepo;
    private final VehicleRepository vehicleRepo;  // used to validate vehicle exists before logging maintenance

    public MaintenanceController(MaintenanceRepository maintenanceRepo, VehicleRepository vehicleRepo) {
        this.maintenanceRepo = maintenanceRepo;
        this.vehicleRepo = vehicleRepo;
    }

    /**
     * POST /api/maintenance
     * Logs a new maintenance record for a vehicle.
     *
     * @Valid         — triggers validation on MaintenanceRequest fields.
     * @RequestBody   — Spring deserializes the incoming JSON body into a MaintenanceRequest.
     * @ResponseStatus(CREATED) — returns HTTP 201 on success.
     *
     * Returns 404 Not Found if the vehicle does not exist.
     *
     * @param req the maintenance data from the request body
     * @throws IllegalArgumentException if the vehicle is not found
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void log(@Valid @RequestBody MaintenanceRequest req) {
        // Verify the vehicle exists before logging maintenance for it
        if (!vehicleRepo.existsById(req.vehicleId()))
            throw new IllegalArgumentException("Vehicle not found");
        maintenanceRepo.save(req);
    }

    /**
     * GET /api/maintenance/{vehicleId}
     * Returns the full maintenance history for a specific vehicle, newest first.
     *
     * @PathVariable — extracts the {vehicleId} from the URL path.
     *
     * Returns 404 Not Found if the vehicle does not exist.
     *
     * @param vehicleId the ID of the vehicle whose history to retrieve
     * @return a JSON array of Maintenance records for the given vehicle
     * @throws IllegalArgumentException if the vehicle is not found
     */
    @GetMapping("/{vehicleId}")
    public List<Maintenance> history(@PathVariable Long vehicleId) {
        if (!vehicleRepo.existsById(vehicleId))
            throw new IllegalArgumentException("Vehicle not found");
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    /**
     * GET /api/maintenance/upcoming
     * Returns maintenance records scheduled within the next [days] days (default: 30).
     * Also includes records that are already overdue (next_service_date in the past).
     *
     * Optional query parameter:
     *   currentMileage — if provided, also returns records where next_service_mileage
     *                    is <= currentMileage (vehicle has reached the service mileage).
     *
     * @RequestParam(defaultValue = "30") — uses 30 if the "days" param is not in the URL.
     * @Min(1) @Max(365) — validates the "days" param is between 1 and 365.
     *
     * Example URLs:
     *   GET /api/maintenance/upcoming              → next 30 days
     *   GET /api/maintenance/upcoming?days=60      → next 60 days
     *   GET /api/maintenance/upcoming?currentMileage=15000 → also include mileage-based
     *
     * @param days           lookahead window in days (1–365, default 30)
     * @param currentMileage optional current odometer reading
     * @return a list of upcoming/overdue maintenance records
     */
    @GetMapping("/upcoming")
    public List<Maintenance> upcoming(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days,
            @RequestParam(required = false) Integer currentMileage) {
        return maintenanceRepo.findUpcoming(days, currentMileage);
    }

    /**
     * GET /api/maintenance/alerts
     * Returns maintenance records where the vehicle is overdue or due within [days] days (default: 7).
     *
     * This endpoint is intended for dashboard alerts — the manager polls it to see
     * which vehicles urgently need servicing.
     *
     * @param days alert window in days (1–90, default 7)
     * @return a list of alert-worthy maintenance records
     */
    @GetMapping("/alerts")
    public List<Maintenance> alerts(@RequestParam(defaultValue = "7") @Min(1) @Max(90) int days) {
        return maintenanceRepo.findAlerts(days);
    }

    /**
     * Exception handler for IllegalArgumentException thrown by any method in this controller.
     *
     * Determines the HTTP status based on the error message:
     *   - "not found" → 404 Not Found
     *   - anything else → 400 Bad Request
     *
     * @param ex the thrown exception
     * @return a ProblemDetail with the appropriate HTTP status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleError(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
