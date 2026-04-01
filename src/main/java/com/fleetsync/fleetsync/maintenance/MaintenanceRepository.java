package com.fleetsync.fleetsync.maintenance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

/**
 * MaintenanceRepository — handles all SQL database operations for the "maintenance" table.
 *
 * @Repository marks this as a Spring data access component.
 * Spring creates one instance and injects it wherever needed
 * (e.g. MaintenanceController, AiService).
 */
@Repository
public class MaintenanceRepository {

    /** Spring's fluent SQL client — injected from the DataSource in application.properties. */
    private final JdbcClient jdbc;

    public MaintenanceRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Inserts a new maintenance record into the database.
     * "created_at" is not included — the DB sets it automatically via DEFAULT CURRENT_TIMESTAMP.
     *
     * Text blocks (""" ... """) are used for multi-line SQL strings — easier to read than concatenation.
     *
     * @param req the maintenance data from the request body
     */
    public void save(MaintenanceRequest req) {
        jdbc.sql("""
                INSERT INTO maintenance
                    (vehicle_id, date, type, description, cost, next_service_date, next_service_mileage)
                VALUES
                    (:vehicleId, :date, :type, :description, :cost, :nextServiceDate, :nextServiceMileage)
                """)
            .param("vehicleId", req.vehicleId())
            .param("date", req.date())
            .param("type", req.type())
            .param("description", req.description())          // can be null — stored as NULL in DB
            .param("cost", req.cost())
            .param("nextServiceDate", req.nextServiceDate())   // can be null
            .param("nextServiceMileage", req.nextServiceMileage()) // can be null
            .update();
    }

    /**
     * Returns all maintenance records for a specific vehicle, ordered by date (newest first).
     * Used by GET /api/maintenance/{vehicleId} to show a vehicle's full service history.
     *
     * @param vehicleId the vehicle's ID to filter by
     * @return a list of Maintenance records for the given vehicle, newest first
     */
    public List<Maintenance> findByVehicleId(Long vehicleId) {
        return jdbc.sql("SELECT * FROM maintenance WHERE vehicle_id = :vehicleId ORDER BY date DESC, created_at DESC")
                   .param("vehicleId", vehicleId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Returns all maintenance records across all vehicles.
     * Used by AiService to provide fleet maintenance context to the AI model.
     *
     * Limited to 200 records to keep the AI prompt size manageable and avoid
     * exceeding OpenAI's context window token limit.
     *
     * @return up to 200 most recent maintenance records
     */
    public List<Maintenance> findAll() {
        return jdbc.sql("SELECT * FROM maintenance ORDER BY date DESC, created_at DESC LIMIT 200")
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Returns maintenance records whose next service is due within the next [daysAhead] days
     * (including today and overdue records where the date has already passed).
     *
     * DATE_ADD(CURDATE(), INTERVAL :daysAhead DAY) calculates the cutoff date.
     * e.g. if today is 2025-01-01 and daysAhead=30, cutoff = 2025-01-31.
     * Records with next_service_date <= cutoff are returned (includes overdue ones).
     *
     * If currentMileage is provided, also returns records where next_service_mileage
     * is <= currentMileage (the vehicle has reached or passed the scheduled mileage).
     *
     * @param daysAhead      number of days to look ahead (1–365)
     * @param currentMileage optional current odometer reading; null to skip mileage check
     * @return a list of upcoming/overdue maintenance records, ordered by next_service_date
     */
    public List<Maintenance> findUpcoming(int daysAhead, Integer currentMileage) {
        if (currentMileage != null) {
            // Include both date-based AND mileage-based upcoming records
            return jdbc.sql("""
                    SELECT * FROM maintenance
                    WHERE (
                        next_service_date IS NOT NULL
                        AND next_service_date <= DATE_ADD(CURDATE(), INTERVAL :daysAhead DAY)
                    )
                    OR (next_service_mileage IS NOT NULL AND next_service_mileage <= :currentMileage)
                    ORDER BY next_service_date ASC
                    """)
                .param("daysAhead", daysAhead)
                .param("currentMileage", currentMileage)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
        }
        // No mileage provided — only return date-based upcoming records
        return jdbc.sql("""
                SELECT * FROM maintenance
                WHERE next_service_date IS NOT NULL
                  AND next_service_date <= DATE_ADD(CURDATE(), INTERVAL :daysAhead DAY)
                ORDER BY next_service_date ASC
                """)
            .param("daysAhead", daysAhead)
            .query((rs, rowNum) -> mapRow(rs))
            .list();
    }

    /**
     * Returns maintenance records where the vehicle is overdue or due within [alertDays] days.
     * Used by GET /api/maintenance/alerts to surface urgent service reminders.
     *
     * This is similar to findUpcoming but uses a shorter default window (7 days)
     * and is intended for dashboard alerts rather than general scheduling queries.
     *
     * @param alertDays number of days for the alert window (1–90)
     * @return a list of alert-worthy maintenance records, ordered by next_service_date
     */
    public List<Maintenance> findAlerts(int alertDays) {
        return jdbc.sql("""
                SELECT * FROM maintenance
                WHERE next_service_date IS NOT NULL
                  AND next_service_date <= DATE_ADD(CURDATE(), INTERVAL :alertDays DAY)
                ORDER BY next_service_date ASC
                """)
                   .param("alertDays", alertDays)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    /**
     * Shared helper that maps a single ResultSet row to a Maintenance record.
     * Extracted to avoid repeating the same mapping code in every query method.
     *
     * next_service_date can be null (not all records have a scheduled next service),
     * so we check for null before calling toLocalDate() to avoid NullPointerException.
     *
     * rs.getObject("next_service_mileage", Integer.class) safely returns null
     * if the column value is SQL NULL (unlike rs.getInt() which returns 0 for NULL).
     *
     * @param rs the current ResultSet row
     * @return a Maintenance record populated from the row's column values
     * @throws java.sql.SQLException if a column cannot be read
     */
    private Maintenance mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Date nextServiceDateSql = rs.getDate("next_service_date");  // null if not set
        return new Maintenance(
            rs.getLong("id"),
            rs.getLong("vehicle_id"),
            rs.getDate("date").toLocalDate(),
            rs.getString("type"),
            rs.getString("description"),                                          // can be null
            rs.getBigDecimal("cost"),
            nextServiceDateSql != null ? nextServiceDateSql.toLocalDate() : null, // safely convert or keep null
            rs.getObject("next_service_mileage", Integer.class),                  // null-safe Integer read
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
