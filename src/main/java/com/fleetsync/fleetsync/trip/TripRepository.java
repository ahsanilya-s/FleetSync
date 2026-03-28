package com.fleetsync.fleetsync.trip;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Handles all SQL operations for the trips table
@Repository
public class TripRepository {

    private final JdbcClient jdbc;

    public TripRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Checks if the given driver already has an active trip (SCHEDULED or IN_PROGRESS)
    // A driver can only be on one trip at a time
    public boolean driverHasActiveTrip(Long driverId) {
        return jdbc.sql("SELECT COUNT(*) FROM trips WHERE driver_id = :driverId AND status IN ('SCHEDULED', 'IN_PROGRESS')")
                   .param("driverId", driverId)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Checks if the given vehicle is already assigned to an active trip
    // A vehicle can only be used in one trip at a time
    public boolean vehicleHasActiveTrip(Long vehicleId) {
        return jdbc.sql("SELECT COUNT(*) FROM trips WHERE vehicle_id = :vehicleId AND status IN ('SCHEDULED', 'IN_PROGRESS')")
                   .param("vehicleId", vehicleId)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Inserts a new trip assignment into the database
    // Status defaults to SCHEDULED as defined in schema.sql
    public void save(TripRequest req) {
        jdbc.sql("INSERT INTO trips (driver_id, vehicle_id, origin, destination) VALUES (:driverId, :vehicleId, :origin, :destination)")
            .param("driverId", req.driverId())
            .param("vehicleId", req.vehicleId())
            .param("origin", req.origin())
            .param("destination", req.destination())
            .update();
    }

    // Finds a single trip by its ID — used for status update validation
    public Optional<Trip> findById(Long id) {
        return jdbc.sql("SELECT * FROM trips WHERE id = :id")
                   .param("id", id)
                   .query((rs, rowNum) -> mapRow(rs))
                   .optional();
    }

    // Updates a trip's status and sets start_time or end_time based on the transition
    public void updateStatus(Long id, String status, LocalDateTime startTime, LocalDateTime endTime) {
        jdbc.sql("UPDATE trips SET status = :status, start_time = COALESCE(:startTime, start_time), end_time = COALESCE(:endTime, end_time) WHERE id = :id")
            .param("status", status)
            .param("startTime", startTime != null ? Timestamp.valueOf(startTime) : null)
            .param("endTime", endTime != null ? Timestamp.valueOf(endTime) : null)
            .param("id", id)
            .update();
    }

    // Returns all trips ordered by creation date (newest first)
    public List<Trip> findAll() {
        return jdbc.sql("SELECT * FROM trips ORDER BY created_at DESC")
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    // Returns trips for a specific driver ordered by creation date (newest first)
    // Used to view trip history for a driver
    public List<Trip> findAllByDriverId(Long driverId) {
        return jdbc.sql("SELECT * FROM trips WHERE driver_id = :driverId ORDER BY created_at DESC")
                   .param("driverId", driverId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    // Returns trips for a specific vehicle ordered by creation date (newest first)
    // Used to view trip history for a vehicle
    public List<Trip> findAllByVehicleId(Long vehicleId) {
        return jdbc.sql("SELECT * FROM trips WHERE vehicle_id = :vehicleId ORDER BY created_at DESC")
                   .param("vehicleId", vehicleId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    // Maps a result set row to a Trip record
    private Trip mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp startTs = rs.getTimestamp("start_time");
        Timestamp endTs   = rs.getTimestamp("end_time");
        return new Trip(
            rs.getLong("id"),
            rs.getLong("driver_id"),
            rs.getLong("vehicle_id"),
            rs.getString("origin"),
            rs.getString("destination"),
            rs.getString("status"),
            startTs != null ? startTs.toLocalDateTime() : null,
            endTs   != null ? endTs.toLocalDateTime()   : null,
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
