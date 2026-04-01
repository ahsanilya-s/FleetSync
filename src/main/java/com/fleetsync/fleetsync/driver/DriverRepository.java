package com.fleetsync.fleetsync.driver;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DriverRepository — handles all SQL database operations for the "drivers" table.
 *
 * @Repository marks this as a Spring data access component.
 * Spring creates one instance and injects it wherever needed
 * (e.g. DriverController, TripController for status updates).
 */
@Repository
public class DriverRepository {

    /** Spring's fluent SQL client — injected from the DataSource in application.properties. */
    private final JdbcClient jdbc;

    public DriverRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Checks if a driver with the given license number already exists.
     * Used during registration to prevent duplicate license numbers.
     *
     * @param licenseNumber the license number to check
     * @return true if the license is already registered, false otherwise
     */
    public boolean existsByLicense(String licenseNumber) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE license_number = :license")
                   .param("license", licenseNumber)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Checks if a driver with the given email already exists.
     * Used during registration to prevent duplicate email addresses.
     *
     * @param email the email address to check
     * @return true if the email is already registered, false otherwise
     */
    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE email = :email")
                   .param("email", email)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Checks if a driver with the given ID exists in the database.
     * Used by TripController before assigning a driver to a trip.
     *
     * @param id the driver ID to check
     * @return true if the driver exists, false otherwise
     */
    public boolean existsById(Long id) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE id = :id")
                   .param("id", id)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Inserts a new driver record into the database.
     * "status" and "created_at" are not included — the DB sets them via DEFAULT constraints.
     * Default status is "AVAILABLE" as defined in schema.sql.
     *
     * @param req the driver data from the request body
     */
    public void save(DriverRequest req) {
        jdbc.sql("INSERT INTO drivers (full_name, license_number, phone, email) VALUES (:name, :license, :phone, :email)")
            .param("name", req.fullName())
            .param("license", req.licenseNumber())
            .param("phone", req.phone())
            .param("email", req.email())
            .update();
    }

    /**
     * Returns all drivers ordered by registration date (newest first).
     *
     * The lambda (rs, rowNum) -> new Driver(...) is a RowMapper.
     * It converts each database row into a Driver record by reading column values.
     *
     * @return a list of all drivers, newest first
     */
    public List<Driver> findAll() {
        return jdbc.sql("SELECT * FROM drivers ORDER BY created_at DESC")
                   .query((rs, rowNum) -> new Driver(
                       rs.getLong("id"),
                       rs.getString("full_name"),
                       rs.getString("license_number"),
                       rs.getString("phone"),
                       rs.getString("email"),
                       rs.getString("status"),                          // "AVAILABLE" or "ON_TRIP"
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }

    /**
     * Updates a driver's operational status.
     * Called by TripController when a trip is created (→ "ON_TRIP") or ends (→ "AVAILABLE").
     *
     * @param id     the ID of the driver to update
     * @param status the new status: "AVAILABLE" or "ON_TRIP"
     */
    public void updateStatus(Long id, String status) {
        jdbc.sql("UPDATE drivers SET status = :status WHERE id = :id")
            .param("status", status)
            .param("id", id)
            .update();
    }
}
