package com.fleetsync.fleetsync.entity;

import jakarta.persistence.*;  // JPA annotations for ORM mapping
import lombok.*;               // Lombok annotations to auto-generate boilerplate code
import java.time.LocalDate;    // For license expiry date (date only, no time)
import java.time.LocalDateTime; // For audit timestamps createdAt / updatedAt
import java.util.List;         // For the one-to-many relationship with Trip

@Entity                        // Marks this class as a JPA-managed database entity
@Table(name = "drivers")       // Maps this entity to the "drivers" table in the DB
@Data                          // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                       // Lombok: enables the builder pattern for object construction
@NoArgsConstructor             // Lombok: generates a no-argument constructor (required by JPA)
@AllArgsConstructor            // Lombok: generates a constructor with all fields as arguments
public class Driver {

    @Id                                                 // Marks 'id' as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID using the DB identity column
    private Long id;

    @Column(nullable = false)   // First name is required
    private String firstName;

    @Column(nullable = false)   // Last name is required
    private String lastName;

    @Column(unique = true, nullable = false) // License number must be unique and non-null across all drivers
    private String licenseNumber;

    @Column(nullable = false)   // Phone number is required for contact purposes
    private String phoneNumber;

    private LocalDate licenseExpiry; // Optional: date when the driver's license expires

    @Column(nullable = false)        // isActive flag is required; controls whether driver can be assigned trips
    private Boolean isActive = true; // Defaults to true — new drivers are active by default

    // Driver has one User account for login
    @OneToOne(fetch = FetchType.LAZY)          // Lazy fetch: User data is not loaded until explicitly accessed
    @JoinColumn(name = "user_id", unique = true) // Creates a FK column "user_id" in the drivers table; unique ensures 1-to-1
    private User user;

    // One driver can have many trips
    @OneToMany(
        mappedBy = "driver",       // Refers to the 'driver' field in the Trip entity (Trip owns the FK)
        cascade = CascadeType.ALL, // Propagates all JPA operations (persist, merge, remove) to related trips
        fetch = FetchType.LAZY     // Trips are NOT loaded from DB until explicitly accessed (performance)
    )
    private List<Trip> trips;      // All trips assigned to this driver

    @Column(updatable = false)     // createdAt is set once on first insert and never modified
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt; // Refreshed on every update to track last modification time

    @PrePersist                      // JPA lifecycle hook: runs automatically before the entity is first saved
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Capture creation timestamp
        updatedAt = LocalDateTime.now(); // Initialise updatedAt at creation time as well
    }

    @PreUpdate                                           // JPA lifecycle hook: runs automatically before every update
    protected void onUpdate() { updatedAt = LocalDateTime.now(); } // Refresh updatedAt on every save
}
