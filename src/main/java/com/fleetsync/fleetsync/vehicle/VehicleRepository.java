package com.fleetsync.fleetsync.vehicle;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VehicleRepository — handles all SQL database operations for the "vehicles" table.
 *
 * @Repository marks this as a Spring data access component.
 * Spring creates one instance and injects it wherever needed (e.g. VehicleController, TripController).
 *
 * We use JdbcClient (Spring 6+) for plain SQL queries — no ORM overhead.
 */
@Repository
public class VehicleRepository {

    /** Spring's fluent SQL client — injected from the DataSource in application.properties. */
    private final JdbcClient jdbc;

    public VehicleRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Checks if any vehicle already has the given plate number.
     * Used when ADDING a new vehicle to prevent duplicate plates.
     *
     * @param plateNumber the plate number to check
     * @return true if the plate is already registered, false otherwise
     */
    public boolean existsByPlateNumber(String plateNumber) {
        return jdbc.sql("SELECT COUNT(*) FROM vehicles WHERE plate_number = :plate")
                   .param("plate", plateNumber)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Checks if any OTHER vehicle (excluding the one being updated) has the given plate number.
     * Used when UPDATING a vehicle — allows keeping the same plate, but rejects stealing another vehicle's plate.
     *
     * The "AND id != :id" clause excludes the current vehicle from the check.
     *
     * @param plateNumber the plate number to check
     * @param excludeId   the ID of the vehicle being updated (excluded from the check)
     * @return true if another vehicle already has this plate, false otherwise
     */
    public boolean existsByPlateNumber(String plateNumber, Long excludeId) {
        return jdbc.sql("SELECT COUNT(*) FROM vehicles WHERE plate_number = :plate AND id != :id")
                   .param("plate", plateNumber)
                   .param("id", excludeId)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Checks if a vehicle with the given ID exists in the database.
     * Used before update/delete operations to return 404 instead of silently doing nothing.
     *
     * @param id the vehicle ID to check
     * @return true if the vehicle exists, false otherwise
     */
    public boolean existsById(Long id) {
        return jdbc.sql("SELECT COUNT(*) FROM vehicles WHERE id = :id")
                   .param("id", id)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Inserts a new vehicle record into the database.
     * "status" and "created_at" are not included — the DB sets them via DEFAULT constraints.
     * Default status is "AVAILABLE" as defined in schema.sql.
     *
     * @param req the vehicle data from the request body
     */
    public void save(VehicleRequest req) {
        jdbc.sql("INSERT INTO vehicles (name, type, plate_number, capacity, fuel_type) VALUES (:name, :type, :plate, :capacity, :fuelType)")
            .param("name", req.name())
            .param("type", req.type())
            .param("plate", req.plateNumber())
            .param("capacity", req.capacity())
            .param("fuelType", req.fuelType())
            .update();  // .update() executes INSERT/UPDATE/DELETE statements
    }

    /**
     * Updates an existing vehicle's details by its ID.
     * All fields are updated at once — partial updates are not supported.
     *
     * @param id  the ID of the vehicle to update
     * @param req the new vehicle data from the request body
     */
    public void update(Long id, VehicleRequest req) {
        jdbc.sql("UPDATE vehicles SET name = :name, type = :type, plate_number = :plate, capacity = :capacity, fuel_type = :fuelType WHERE id = :id")
            .param("name", req.name())
            .param("type", req.type())
            .param("plate", req.plateNumber())
            .param("capacity", req.capacity())
            .param("fuelType", req.fuelType())
            .param("id", id)
            .update();
    }

    /**
     * Deletes a vehicle record by its ID.
     * The controller verifies the vehicle exists before calling this.
     *
     * @param id the ID of the vehicle to delete
     */
    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM vehicles WHERE id = :id")
            .param("id", id)
            .update();
    }

    /**
     * Updates a vehicle's operational status.
     * Called by TripController when a trip is created (→ "ON_TRIP") or ends (→ "AVAILABLE").
     *
     * @param id     the ID of the vehicle to update
     * @param status the new status: "AVAILABLE" or "ON_TRIP"
     */
    public void updateStatus(Long id, String status) {
        jdbc.sql("UPDATE vehicles SET status = :status WHERE id = :id")
            .param("status", status)
            .param("id", id)
            .update();
    }

    /**
     * Returns all vehicles ordered by creation date (newest first).
     *
     * The lambda (rs, rowNum) -> new Vehicle(...) is a RowMapper.
     * It's called once per row in the result set and converts each row into a Vehicle record.
     * rs is the ResultSet (current row), rowNum is the row index (not used here).
     *
     * @return a list of all vehicles, newest first
     */
    public List<Vehicle> findAll() {
        return jdbc.sql("SELECT * FROM vehicles ORDER BY created_at DESC")
                   .query((rs, rowNum) -> new Vehicle(
                       rs.getLong("id"),
                       rs.getString("name"),
                       rs.getString("type"),
                       rs.getString("plate_number"),
                       rs.getInt("capacity"),
                       rs.getString("fuel_type"),
                       rs.getString("status"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();  // .list() collects all mapped rows into a List<Vehicle>
    }
}
