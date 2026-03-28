package com.fleetsync.fleetsync.driver;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

// Handles all SQL operations for the drivers table
@Repository
public class DriverRepository {

    private final JdbcClient jdbc;

    public DriverRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Checks if a driver with the given license number already exists
    // Used to prevent duplicate license registrations
    public boolean existsByLicense(String licenseNumber) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE license_number = :license")
                   .param("license", licenseNumber)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Checks if a driver with the given email already exists
    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE email = :email")
                   .param("email", email)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Checks if a driver with the given ID exists
    // Used before update/delete operations
    public boolean existsById(Long id) {
        return jdbc.sql("SELECT COUNT(*) FROM drivers WHERE id = :id")
                   .param("id", id)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Inserts a new driver record into the database
    // Status defaults to AVAILABLE as defined in schema.sql
    public void save(DriverRequest req) {
        jdbc.sql("INSERT INTO drivers (full_name, license_number, phone, email) VALUES (:name, :license, :phone, :email)")
            .param("name", req.fullName())
            .param("license", req.licenseNumber())
            .param("phone", req.phone())
            .param("email", req.email())
            .update();
    }

    // Returns all drivers ordered by registration date (newest first)
    // Maps each database row to a Driver record
    public List<Driver> findAll() {
        return jdbc.sql("SELECT * FROM drivers ORDER BY created_at DESC")
                   .query((rs, rowNum) -> new Driver(
                       rs.getLong("id"),
                       rs.getString("full_name"),
                       rs.getString("license_number"),
                       rs.getString("phone"),
                       rs.getString("email"),
                       rs.getString("status"),         // AVAILABLE or ON_TRIP
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }

    // Updates a driver's status — called when a trip is created or completed
    // status values: AVAILABLE, ON_TRIP
    public void updateStatus(Long id, String status) {
        jdbc.sql("UPDATE drivers SET status = :status WHERE id = :id")
            .param("status", status)
            .param("id", id)
            .update();
    }
}
