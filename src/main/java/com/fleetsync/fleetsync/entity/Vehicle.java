package com.fleetsync.fleetsync.entity;

import com.fleetsync.fleetsync.enums.VehicleStatus; // Enum for vehicle availability state (AVAILABLE, IN_USE, etc.)
import com.fleetsync.fleetsync.enums.VehicleType;   // Enum for vehicle category (BUS, VAN, TRUCK, etc.)
import jakarta.persistence.*;             // JPA annotations for ORM mapping
import lombok.*;                          // Lombok annotations to auto-generate boilerplate code
import java.time.LocalDateTime;           // For timestamp fields createdAt / updatedAt
import java.util.List;                    // For the one-to-many relationship with Trip

@Entity                                   // Marks this class as a JPA-managed database entity
@Table(name = "vehicles")                 // Maps this entity to the "vehicles" table in the DB
@Data                                     // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                                  // Lombok: enables the builder pattern for object construction
@NoArgsConstructor                        // Lombok: generates a no-argument constructor (required by JPA)
@AllArgsConstructor                       // Lombok: generates a constructor with all fields as arguments
public class Vehicle {

    @Id                                                    // Marks 'id' as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // Auto-increments the ID using the DB identity column
    private Long id;

    @Column(unique = true, nullable = false, length = 20)  // Plate number must be unique, non-null, max 20 chars
    private String plateNumber;

    @Enumerated(EnumType.STRING)   // Stores the enum name as a String (e.g., "BUS") instead of ordinal
    @Column(nullable = false)      // VehicleType is required
    private VehicleType type;

    @Column(nullable = false)      // Model name is required (e.g., "Toyota Hiace")
    private String model;

    @Column(nullable = false)      // Passenger/cargo capacity is required
    private Integer capacity;

    @Enumerated(EnumType.STRING)                           // Stores status as a readable String in the DB
    @Builder.Default                                       // Ensures the default value is respected when using @Builder
    private VehicleStatus status = VehicleStatus.AVAILABLE; // New vehicles default to AVAILABLE

    // One vehicle can have many trips
    @OneToMany(
        mappedBy = "vehicle",          // Refers to the 'vehicle' field in the Trip entity (owning side)
        cascade = CascadeType.ALL,     // Propagates all operations (persist, remove, etc.) to related trips
        fetch = FetchType.LAZY         // Trips are NOT loaded from DB until explicitly accessed (performance)
    )
    private List<Trip> trips;          // Collection of all trips associated with this vehicle

    @Column(updatable = false)         // createdAt is set once on insert and never changed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;   // Updated every time the entity is modified

    @PrePersist                        // JPA lifecycle hook: runs automatically before the entity is first saved
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set creation timestamp
        updatedAt = LocalDateTime.now(); // Also initialise updatedAt on creation
    }

    @PreUpdate                                          // JPA lifecycle hook: runs automatically before every update
    protected void onUpdate() { updatedAt = LocalDateTime.now(); } // Refresh updatedAt on every save
}
