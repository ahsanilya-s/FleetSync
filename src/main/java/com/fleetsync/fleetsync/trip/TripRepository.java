package com.fleetsync.fleetsync.trip;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TripRepository — handles all SQL database operations for the "trips" table.
 *
 * @Repository marks this as a Spring data access component.
 * Spring creates one instance and injects it wherever needed (e.g. TripController, AiService).
 */
@Repository
public class TripRepository {

    /** Spring's fluent SQL client — injected from the DataSource in application.properties. */
    private final JdbcClient jdbc;

    public TripRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Checks if the given driver already has an active trip (SCHEDULED or IN_PROGRESS).
     * A driver can only be on one trip at a time — this prevents double-booking.
     *
     * SQL: IN ('SCHEDULED', 'IN_PROGRESS') matches either active status.
     *
     * @param driverId the driver's ID to check
     * @return true if the driver has an active trip, false otherwise
     */
    public boolean driverHasActiveTrip(Long driverId) {
        return jdbc.sql("SELECT COUNT(*) FROM trips WHERE driver_id = :driverId AND status IN ('SCHEDULED', 'IN_PROGRESS')")
                   .param("driverId", driverId)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Checks if the given vehicle is already assigned to an active trip.
     * A vehicle can only be used in one trip at a time — this prevents double-booking.
     *
     * @param vehicleId the vehicle's ID to check
     * @return true if the vehicle has an active trip, false otherwise
     */
    public boolean vehicleHasActiveTrip(Long vehicleId) {
        return jdbc.sql("SELECT COUNT(*) FROM trips WHERE vehicle_id = :vehicleId AND status IN ('SCHEDULED', 'IN_PROGRESS')")
                   .param("vehicleId", vehicleId)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Inserts a new trip assignment into the database.
     * "status" defaults to "SCHEDULED" and "created_at" is set automatically by the DB.
     * start_time and end_time are null until the trip progresses.
     *
     * @param req the trip data from the request body
     */
    public void save(TripRequest req) {
        jdbc.sql("INSERT INTO trips (driver_id, vehicle_id, origin, destination) VALUES (:driverId, :vehicleId, :origin, :destination)")
            .param("driverId", req.driverId())
            .param("vehicleId", req.vehicleId())
            .param("origin", req.origin())
            .param("destination", req.destination())
            .update();
    }

    /**
     * Finds a single trip by its ID.
     * Returns Optional.empty() if no trip exists with the given ID.
     * Used by TripController to load the current trip before validating a status transition.
     *
     * @param id the trip ID to look up
     * @return an Optional containing the Trip if found, or empty if not found
     */
    public Optional<Trip> findById(Long id) {
        return jdbc.sql("SELECT * FROM trips WHERE id = :id")
                   .param("id", id)
                   .query((rs, rowNum) -> mapRow(rs))  // reuse the shared mapRow helper
                   .optional();
    }

    /**
     * Updates a trip's status and optionally sets start_time or end_time.
     *
     * COALESCE(:startTime, start_time) means:
     *   - If :startTime is not null → use the new value
     *   - If :startTime is null     → keep the existing value in the DB
     * This allows us to set start_time only when transitioning to IN_PROGRESS,
     * and end_time only when transitioning to COMPLETED or CANCELLED.
     *
     * We convert LocalDateTime to java.sql.Timestamp because JDBC requires it.
     *
     * @param id        the trip ID to update
     * @param status    the new status value
     * @param startTime set when transitioning to IN_PROGRESS, null otherwise
     * @param endTime   set when transitioning to COMPLETED or CANCELLED, null otherwise
     */
    public void updateStatus(Long id, String status, LocalDateTime startTime, LocalDateTime endTime) {
        jdbc.sql("UPDATE trips SET status = :status, start_time = COALESCE(:startTime, start_time), end_time = COALESCE(:endTime, end_time) WHERE id = :id")
            .param("status", status)
            .param("startTime", startTime != null ? Timestamp.valueOf(startTime) : null)  // convert to SQL Timestamp
            .param("endTime",   endTime   != null ? Timestamp.valueOf(endTime)   : null)
            .param("id", id)
            .update();
    }

    /**
     * Returns all trips ordered by creation date (newest first).
     * Used by the manager to see the full trip history and by AiService for fleet context.
     *
     * @return a list of all trips, newest first
     */
    public List<Trip> findAll() {
        return jdbc.sql("SELECT * FROM trips ORDER BY created_at DESC")
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Returns all trips for a specific driver, ordered by creation date (newest first).
     * Used when a driver or manager wants to view a driver's trip history.
     *
     * @param driverId the driver's ID to filter by
     * @return a list of trips for the given driver, newest first
     */
    public List<Trip> findAllByDriverId(Long driverId) {
        return jdbc.sql("SELECT * FROM trips WHERE driver_id = :driverId ORDER BY created_at DESC")
                   .param("driverId", driverId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Returns all trips for a specific vehicle, ordered by creation date (newest first).
     * Used when a manager wants to view a vehicle's trip history.
     *
     * @param vehicleId the vehicle's ID to filter by
     * @return a list of trips for the given vehicle, newest first
     */
    public List<Trip> findAllByVehicleId(Long vehicleId) {
        return jdbc.sql("SELECT * FROM trips WHERE vehicle_id = :vehicleId ORDER BY created_at DESC")
                   .param("vehicleId", vehicleId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Shared helper that maps a single ResultSet row to a Trip record.
     * Extracted to avoid repeating the same mapping code in every query method.
     *
     * start_time and end_time can be null (trip hasn't started/ended yet),
     * so we check for null before calling toLocalDateTime() to avoid NullPointerException.
     *
     * @param rs the current ResultSet row
     * @return a Trip record populated from the row's column values
     * @throws java.sql.SQLException if a column cannot be read
     */
    private Trip mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp startTs = rs.getTimestamp("start_time");  // null if trip hasn't started
        Timestamp endTs   = rs.getTimestamp("end_time");    // null if trip hasn't ended
        return new Trip(
            rs.getLong("id"),
            rs.getLong("driver_id"),
            rs.getLong("vehicle_id"),
            rs.getString("origin"),
            rs.getString("destination"),
            rs.getString("status"),
            startTs != null ? startTs.toLocalDateTime() : null,  // safely convert or keep null
            endTs   != null ? endTs.toLocalDateTime()   : null,
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
