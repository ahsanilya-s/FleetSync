package com.fleetsync.fleetsync.trip;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    // Returns all trips ordered by creation date (newest first)
    public List<Trip> findAll() {
        return jdbc.sql("SELECT * FROM trips ORDER BY created_at DESC")
                   .query((rs, rowNum) -> new Trip(
                       rs.getLong("id"),
                       rs.getLong("driver_id"),
                       rs.getLong("vehicle_id"),
                       rs.getString("origin"),
                       rs.getString("destination"),
                       rs.getString("status"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }
}
