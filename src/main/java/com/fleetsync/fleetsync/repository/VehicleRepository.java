package com.fleetsync.fleetsync.repository;

import com.fleetsync.fleetsync.entity.User;
import com.fleetsync.fleetsync.entity.Vehicle;
import com.fleetsync.fleetsync.enums.VehicleStatus;
import com.fleetsync.fleetsync.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vehicle entity database operations.
 * 
 * Extends JpaRepository<Vehicle, Long> which provides:
 * - Vehicle: The entity type this repository manages
 * - Long: The type of the entity's primary key (@Id field)
 * 
 * Behind the scenes:
 * - Spring Data JPA creates a proxy implementation at runtime (no need to write implementation class)
 * - Inherits 20+ CRUD methods: save(), findById(), findAll(), delete(), count(), etc.
 * - Automatically manages transactions via @Transactional on all methods
 * - Uses EntityManager and Hibernate under the hood to execute SQL queries
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Finds a vehicle by its unique plate number.
     * 
     * Method naming convention: findBy + PropertyName
     * Spring Data JPA parses the method name and generates:
     * SQL: SELECT * FROM vehicle WHERE plate_number = ?
     * 
     * Returns Optional to handle cases where no vehicle is found (null-safe)
     */
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    
    /**
     * Finds all vehicles of a specific type (TRUCK, BIKE, SHIP, TAXI, VAN).
     * 
     * Generated SQL: SELECT * FROM vehicle WHERE type = ?
     * Spring automatically converts VehicleType enum to its string/ordinal value
     */
    List<Vehicle> findByType(VehicleType type);
    
    /**
     * Finds all vehicles with a specific status (AVAILABLE, ON_TRIP, MAINTENANCE, RETIRED).
     * 
     * Generated SQL: SELECT * FROM vehicle WHERE status = ?
     * Useful for filtering vehicles by their current operational state
     */
    List<Vehicle> findByStatus(VehicleStatus status);
    
    /**
     * Checks if a vehicle with the given plate number already exists.
     * 
     * Method naming convention: existsBy + PropertyName
     * Generated SQL: SELECT COUNT(*) > 0 FROM vehicle WHERE plate_number = ?
     * 
     * More efficient than findByPlateNumber() when you only need to check existence
     * Returns boolean instead of fetching the entire entity
     */
    boolean existsByPlateNumber(String plateNumber);
    
    /**
     * Finds all vehicles that are currently available for assignment.
     * 
     * @Query annotation: Allows writing custom JPQL (Java Persistence Query Language)
     * - JPQL uses entity names (Vehicle) and property names (status), not table/column names
     * - Spring translates this to SQL: SELECT * FROM vehicle WHERE status = 'AVAILABLE'
     * - Useful when method naming conventions become too complex or unclear
     * 
     * Alternative without @Query: findByStatus(VehicleStatus.AVAILABLE)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE'")
    List<Vehicle> findAllAvailable();

}
