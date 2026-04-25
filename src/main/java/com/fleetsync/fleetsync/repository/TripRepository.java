package com.fleetsync.fleetsync.repository;

import com.fleetsync.fleetsync.entity.Trip;
import com.fleetsync.fleetsync.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByDriverId(Long driverId);
    Optional<Trip> findByVehicleId(Long vehicleId);
    Optional<Trip> findByStatus(String status);
    Optional<Trip> findByDriverIdAndStatus(Long driverId, TripStatus status);
}
