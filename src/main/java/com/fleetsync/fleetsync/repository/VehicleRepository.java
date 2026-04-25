package com.fleetsync.fleetsync.repository;

import com.fleetsync.fleetsync.entity.User;
import com.fleetsync.fleetsync.entity.Vehicle;
import com.fleetsync.fleetsync.enums.VehicleStatus;
import com.fleetsync.fleetsync.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByType(VehicleType type);
    List<Vehicle> findByStatus(VehicleStatus status);
    boolean existsByPlateNumber(String plateNumber);
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE'")
    List<Vehicle> findAllAvailable();


}
