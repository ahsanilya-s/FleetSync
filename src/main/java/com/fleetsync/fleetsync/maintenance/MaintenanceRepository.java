package com.fleetsync.fleetsync.maintenance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
        jdbc.sql("INSERT INTO maintenance (vehicle_id, date, type, description, cost) VALUES (:vehicleId, :date, :type, :description, :cost)")
            .param("vehicleId", req.vehicleId())
            .param("date", req.date())
            .param("type", req.type())
            .param("description", req.description())
            .param("cost", req.cost())
            .update();
    }

    // Returns all maintenance records for a specific vehicle ordered by date (newest first)
    public List<Maintenance> findByVehicleId(Long vehicleId) {
        return jdbc.sql("SELECT * FROM maintenance WHERE vehicle_id = :vehicleId ORDER BY date DESC, created_at DESC")
                   .param("vehicleId", vehicleId)
                   .query((rs, rowNum) -> new Maintenance(
                       rs.getLong("id"),
                       rs.getLong("vehicle_id"),
                       rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                       rs.getString("type"),
                       rs.getString("description"),
                       rs.getBigDecimal("cost"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }

    // Returns all maintenance records across all vehicles — used by the AI service for fleet context.
    // Limited to the 200 most recent records to keep the AI prompt size manageable.
    public List<Maintenance> findAll() {
        return jdbc.sql("SELECT * FROM maintenance ORDER BY date DESC, created_at DESC LIMIT 200")
                   .query((rs, rowNum) -> new Maintenance(
                       rs.getLong("id"),
                       rs.getLong("vehicle_id"),
                       rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                       rs.getString("type"),
                       rs.getString("description"),
                       rs.getBigDecimal("cost"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }
}
