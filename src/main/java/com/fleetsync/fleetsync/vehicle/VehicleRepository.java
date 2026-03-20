package com.fleetsync.fleetsync.vehicle;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepository {

    private final JdbcClient jdbc;

    public VehicleRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsByPlateNumber(String plateNumber) {
        return jdbc.sql("SELECT COUNT(*) FROM vehicles WHERE plate_number = :plate")
                   .param("plate", plateNumber)
                   .query(Integer.class)
                   .single() > 0;
    }

    public boolean existsById(Long id) {
        return jdbc.sql("SELECT COUNT(*) FROM vehicles WHERE id = :id")
                   .param("id", id)
                   .query(Integer.class)
                   .single() > 0;
    }

    public void save(VehicleRequest req) {
        jdbc.sql("INSERT INTO vehicles (name, type, plate_number, capacity) VALUES (:name, :type, :plate, :capacity)")
            .param("name", req.name())
            .param("type", req.type())
            .param("plate", req.plateNumber())
            .param("capacity", req.capacity())
            .update();
    }

    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM vehicles WHERE id = :id")
            .param("id", id)
            .update();
    }

    public List<Vehicle> findAll() {
        return jdbc.sql("SELECT * FROM vehicles ORDER BY created_at DESC")
                   .query((rs, _) -> new Vehicle(
                       rs.getLong("id"),
                       rs.getString("name"),
                       rs.getString("type"),
                       rs.getString("plate_number"),
                       rs.getInt("capacity"),
                       rs.getString("status"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .list();
    }
}
