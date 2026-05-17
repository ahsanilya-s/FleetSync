package com.fleetsync.fleetsync.repository;

import com.fleetsync.fleetsync.entity.Trip;
import com.fleetsync.fleetsync.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Trip entity database operations.
 * 
 * Extends JpaRepository to inherit standard CRUD operations:
 * - findById(Long id) - Find trip by ID
 * - findAll() - Get all trips
 * - save(Trip trip) - Create or update trip
 * - deleteById(Long id) - Delete trip by ID
 * - count() - Count total trips
 * 
 * Spring Data JPA automatically implements this interface at runtime.
 * Custom query methods are generated based on method naming conventions.
 * 
 * @author FleetSync Team
 * @see Trip
 * @see JpaRepository
 */
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    /**
     * Find a trip by driver ID.
     * 
     * Spring Data JPA generates query:
     * SELECT * FROM trip WHERE driver_id = ?
     * 
     * @param driverId the ID of the driver
     * @return Optional containing the trip if found, empty otherwise
     */
    Optional<Trip> findByDriverId(Long driverId);
    
    /**
     * Find a trip by vehicle ID.
     * 
     * Spring Data JPA generates query:
     * SELECT * FROM trip WHERE vehicle_id = ?
     * 
     * @param vehicleId the ID of the vehicle
     * @return Optional containing the trip if found, empty otherwise
     */
    Optional<Trip> findByVehicleId(Long vehicleId);
    
    /**
     * Find a trip by status.
     * 
     * Spring Data JPA generates query:
     * SELECT * FROM trip WHERE status = ?
     * 
     * @param status the status of the trip (as String)
     * @return Optional containing the trip if found, empty otherwise
     */
    Optional<Trip> findByStatus(String status);
    
    /**
     * Find a trip by driver ID and status.
     * 
     * Useful for checking if a driver has an active trip.
     * Spring Data JPA generates query:
     * SELECT * FROM trip WHERE driver_id = ? AND status = ?
     * 
     * @param driverId the ID of the driver
     * @param status the status of the trip (TripStatus enum)
     * @return Optional containing the trip if found, empty otherwise
     */
    Optional<Trip> findByDriverIdAndStatus(Long driverId, TripStatus status);
}
