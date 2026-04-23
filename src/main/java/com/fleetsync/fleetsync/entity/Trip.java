package com.fleetsync.fleetsync.entity;

import com.fleetsync.fleetsync.enums.TripStatus; // Enum for trip lifecycle state (PENDING, IN_PROGRESS, COMPLETED, etc.)
import jakarta.persistence.*;                     // JPA annotations for ORM mapping
import lombok.*;                                  // Lombok annotations to auto-generate boilerplate code
import java.time.LocalDateTime;                   // For all timestamp fields on this entity

@Entity                        // Marks this class as a JPA-managed database entity
@Table(name = "trips")         // Maps this entity to the "trips" table in the DB
@Data                          // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                       // Lombok: enables the builder pattern for object construction
@NoArgsConstructor             // Lombok: generates a no-argument constructor (required by JPA)
@AllArgsConstructor            // Lombok: generates a constructor with all fields as arguments
public class Trip {

    @Id                                                 // Marks 'id' as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID using the DB identity column
    private Long id;

    @Column(nullable = false)   // Origin location is required
    private String origin;

    @Column(nullable = false)   // Destination location is required
    private String destination;

    @Enumerated(EnumType.STRING)                      // Stores the enum name as a String (e.g., "PENDING") in the DB
    @Builder.Default                                  // Ensures the default value is respected when using @Builder
    private TripStatus status = TripStatus.PENDING;   // New trips start in PENDING state by default

    private String notes; // Optional free-text notes about the trip (instructions, remarks, etc.)

    // Many trips -> one driver
    @ManyToOne(fetch = FetchType.LAZY)    // Lazy fetch: Driver data is not loaded until explicitly accessed
    @JoinColumn(name = "driver_id")       // Creates FK column "driver_id" in the trips table pointing to drivers.id
    private Driver driver;                // The driver assigned to carry out this trip

    // Many trips -> one vehicle
    @ManyToOne(fetch = FetchType.LAZY)    // Lazy fetch: Vehicle data is not loaded until explicitly accessed
    @JoinColumn(name = "vehicle_id")      // Creates FK column "vehicle_id" in the trips table pointing to vehicles.id
    private Vehicle vehicle;              // The vehicle used for this trip

    // Manager who created this trip
    @ManyToOne(fetch = FetchType.LAZY)    // Lazy fetch: User data is not loaded until explicitly accessed
    @JoinColumn(name = "created_by")      // Creates FK column "created_by" in the trips table pointing to users.id
    private User createdBy;               // The manager/user who scheduled this trip

    @Column(updatable = false)            // createdAt is set once on first insert and never modified
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;      // Timestamp recorded when the trip status changes to IN_PROGRESS

    private LocalDateTime completedAt;    // Timestamp recorded when the trip status changes to COMPLETED

    @PrePersist                           // JPA lifecycle hook: runs automatically before the entity is first saved
    protected void onCreate() { createdAt = LocalDateTime.now(); } // Capture creation timestamp on insert
}
