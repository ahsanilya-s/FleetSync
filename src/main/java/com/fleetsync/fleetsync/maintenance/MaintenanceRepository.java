package com.fleetsync.fleetsync.maintenance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

// Handles all SQL operations for the maintenance table
@Repository
public class MaintenanceRepository {

    private final JdbcClient jdbc;

    public MaintenanceRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Inserts a new maintenance record linked to the given vehicle
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
            .param("description", req.description())
            .param("cost", req.cost())
            .param("nextServiceDate", req.nextServiceDate())
            .param("nextServiceMileage", req.nextServiceMileage())
            .update();
    }

    // Returns all maintenance records for the given vehicle, newest first
    public List<Maintenance> findByVehicleId(Long vehicleId) {
        return jdbc.sql("SELECT * FROM maintenance WHERE vehicle_id = :vehicleId ORDER BY date DESC, created_at DESC")
                   .param("vehicleId", vehicleId)
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    // Returns all maintenance records across all vehicles — used by the AI service for fleet context.
    // Limited to the 200 most recent records to keep the AI prompt size manageable.
    public List<Maintenance> findAll() {
        return jdbc.sql("SELECT * FROM maintenance ORDER BY date DESC, created_at DESC LIMIT 200")
                   .query((rs, rowNum) -> mapRow(rs))
                   .list();
    }

    // Returns maintenance records whose next service date falls within the next :daysAhead days
    // (includes today). Also includes any records that are already overdue (next_service_date in
    // the past). If currentMileage is provided, additionally returns records where the vehicle
    // mileage has reached or exceeded the scheduled service mileage.
    public List<Maintenance> findUpcoming(int daysAhead, Integer currentMileage) {
        if (currentMileage != null) {
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

    // Returns maintenance records where the vehicle is overdue or due within :alertDays days
    // These are used to surface service alerts to the fleet manager
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

    // Maps a ResultSet row to a Maintenance record
    private Maintenance mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Date nextServiceDateSql = rs.getDate("next_service_date");
        return new Maintenance(
            rs.getLong("id"),
            rs.getLong("vehicle_id"),
            rs.getDate("date").toLocalDate(),
            rs.getString("type"),
            rs.getString("description"),
            rs.getBigDecimal("cost"),
            nextServiceDateSql != null ? nextServiceDateSql.toLocalDate() : null,
            rs.getObject("next_service_mileage", Integer.class),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
