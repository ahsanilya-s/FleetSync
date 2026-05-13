# FleetSync 2.0 — Fleet Management System

> A **Spring Boot REST API** for managing vehicles, drivers, and trips. Built with Java 17, Spring Boot 4, MySQL, JPA/Hibernate, Lombok, and Jakarta Bean Validation.

---

## Table of Contents

- [Overview](#overview)
- [Development Status](#development-status)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [Enums Reference](#enums-reference)
- [API Endpoints](#api-endpoints)
- [Error Handling](#error-handling)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#database-setup)
  - [Configuration](#configuration)
  - [Run the Application](#run-the-application)
- [DTO Reference](#dto-reference)
- [Security](#security)
- [Execution Guide Highlights](#execution-guide-highlights)
- [Contributing](#contributing)

---

## Overview

**FleetSync 2.0** is a backend fleet management platform that enables organisations to track and coordinate their entire vehicle fleet from a single REST API. Managers can register vehicles, onboard drivers, schedule trips, and monitor trip lifecycle states.

---

## Development Status

| Area | Status |
|---|---|
| JPA Entities (User, Vehicle, Driver, Trip) | ✅ Complete |
| Enums (Role, VehicleType, VehicleStatus, TripStatus) | ✅ Complete |
| DTOs (all request & response DTOs) | ✅ Complete |
| Repositories (all four) | ✅ Complete |
| VehicleService (CRUD + filtering) | ✅ Complete |
| Global Exception Handling | ✅ Complete |
| Bean Validation on all DTOs | ✅ Complete |
| Driver / Trip / User Services | 🔧 In Progress |
| REST Controllers (Driver, Trip, User, Vehicle) | 🔧 In Progress |
| JWT Authentication & Spring Security | 📋 Planned |

---

## Key Features

- **Vehicle Management** — Register, update, and retrieve vehicles with real-time status tracking (`AVAILABLE`, `ON_TRIP`, `MAINTENANCE`, `RETIRED`); filter by type or status
- **Driver Management** — Onboard drivers, link them to user accounts, and track license expiry dates
- **Trip Scheduling** — Create and manage trips with full lifecycle support (`PENDING` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`)
- **Role-Based Access Control** — Three roles defined: `ADMIN`, `MANAGER`, `DRIVER` (JWT enforcement planned)
- **Audit Timestamps** — Automatic `createdAt` / `updatedAt` tracking on every entity via JPA lifecycle hooks
- **Bean Validation** — Input validation on all DTOs using Jakarta Validation annotations
- **Centralised Error Handling** — `GlobalExceptionHandler` returns consistent JSON error responses for `404`, `409`, `400`, and validation failures
- **Lazy Loading** — Optimised database queries with `FetchType.LAZY` on all relationships

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8+ |
| Security | JWT (planned — not yet wired) |
| Build Tool | Maven (Maven Wrapper included) |
| Boilerplate Reduction | Lombok |
| Validation | Jakarta Bean Validation |
| Server | Embedded Tomcat (port 8080) |

---

## Project Structure

```
FleetSync/
├── src/
│   ├── main/
│   │   ├── java/com/fleetsync/fleetsync/
│   │   │   ├── Application.java                   # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── TestController.java            # Health-check endpoints
│   │   │   ├── dto/                               # Request / Response DTOs
│   │   │   │   ├── DriverRequestDto.java
│   │   │   │   ├── DriverResponseDto.java
│   │   │   │   ├── TripRequestDto.java
│   │   │   │   ├── TripResponseDto.java
│   │   │   │   ├── UserRequestDto.java
│   │   │   │   ├── UserResponseDto.java
│   │   │   │   ├── VehicleRequestDto.java
│   │   │   │   └── VehicleResponseDto.java
│   │   │   ├── entity/                            # JPA entities
│   │   │   │   ├── Driver.java
│   │   │   │   ├── Trip.java
│   │   │   │   ├── User.java
│   │   │   │   └── Vehicle.java
│   │   │   ├── enums/                             # Domain enumerations
│   │   │   │   ├── Role.java
│   │   │   │   ├── TripStatus.java
│   │   │   │   ├── VehicleStatus.java
│   │   │   │   └── VehicleType.java
│   │   │   ├── exception/                         # Global exception handling
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/                        # Spring Data JPA repositories
│   │   │   │   ├── DriverRepository.java
│   │   │   │   ├── TripRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── VehicleRepository.java
│   │   │   ├── security/                          # Placeholder for future JWT filter
│   │   │   └── service/
│   │   │       └── VehicleService.java            # Vehicle CRUD business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/fleetsync/fleetsync/
│           └── ApplicationTests.java
├── pom.xml
├── mvnw / mvnw.cmd                                # Maven Wrapper scripts
└── FleetSyncExecution_Guide.pdf
```

---

## Domain Model

### User
Represents a system user with login credentials and a role.

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | PK, auto-generated |
| `username` | String | Unique, not null, max 50 chars |
| `email` | String | Unique, not null, max 100 chars |
| `password` | String | Not null, max 100 chars |
| `role` | Role (enum) | Not null |
| `isActive` | Boolean | Default `true` |
| `createdAt` | LocalDateTime | Set on insert, immutable |
| `updatedAt` | LocalDateTime | Refreshed on every update |

### Vehicle
Represents a fleet vehicle that can be assigned to trips.

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | PK, auto-generated |
| `plateNumber` | String | Unique, not null, max 20 chars |
| `type` | VehicleType (enum) | Not null |
| `model` | String | Not null |
| `capacity` | Integer | Not null |
| `status` | VehicleStatus (enum) | Default `AVAILABLE` |
| `trips` | List\<Trip\> | One-to-many, lazy |
| `createdAt` | LocalDateTime | Set on insert, immutable |
| `updatedAt` | LocalDateTime | Refreshed on every update |

### Driver
Represents a driver who can be assigned to trips.

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | PK, auto-generated |
| `firstName` | String | Not null |
| `lastName` | String | Not null |
| `licenseNumber` | String | Unique, not null |
| `phoneNumber` | String | Not null |
| `licenseExpiry` | LocalDate | Optional |
| `isActive` | Boolean | Default `true` |
| `user` | User | One-to-one, lazy |
| `trips` | List\<Trip\> | One-to-many, lazy |

### Trip
Represents a scheduled journey from origin to destination.

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | PK, auto-generated |
| `origin` | String | Not null |
| `destination` | String | Not null |
| `status` | TripStatus (enum) | Default `PENDING` |
| `notes` | String | Optional |
| `driver` | Driver | Many-to-one, lazy |
| `vehicle` | Vehicle | Many-to-one, lazy |
| `createdBy` | User | Many-to-one, lazy |
| `createdAt` | LocalDateTime | Set on insert, immutable |
| `startedAt` | LocalDateTime | Set when trip starts |
| `completedAt` | LocalDateTime | Set when trip completes |

---

## Enums Reference

### Role
```
ADMIN | MANAGER | DRIVER
```

### VehicleType
```
TRUCK | BIKE | SHIP | TAXI | VAN
```

### VehicleStatus
```
AVAILABLE | ON_TRIP | MAINTENANCE | RETIRED
```

### TripStatus
```
PENDING | IN_PROGRESS | COMPLETED | CANCELLED
```

---

## API Endpoints

### Health Check
| Method | URL | Description |
|---|---|---|
| GET | `/api/test/hello` | Returns `"Hello, mister!"` |
| GET | `/api/test/sheikh` | Returns `"Hello, Sheikh!"` |

### Vehicles *(service layer complete; controller endpoints in progress)*
| Operation | Description |
|---|---|
| Create vehicle | Validates unique plate number; plate is uppercased automatically |
| Get all vehicles | Returns all vehicles as a list |
| Get vehicles by type | Filter by `VehicleType` enum value |
| Get available vehicles | Custom JPQL query — returns all `AVAILABLE` vehicles |
| Get vehicles by status | Filter by `VehicleStatus` enum value |
| Update vehicle | Updates plate, model, capacity, and type; enforces plate uniqueness |
| Find by plate number | Lookup a single vehicle by its plate number |

> Driver, Trip, and User CRUD endpoints are being implemented alongside their service layers.

---

## Error Handling

All errors return a consistent JSON structure managed by `GlobalExceptionHandler`:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Vehicle not found with id: 5"
}
```

### HTTP Status Codes

| Exception | HTTP Status | Trigger |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entity looked up by ID does not exist |
| `DuplicateResourceException` | `409 Conflict` | Creating a resource with a duplicate unique field (e.g. plate number) |
| `BusinessException` | `400 Bad Request` | Business rule violated (e.g. assigning a RETIRED vehicle) |
| `MethodArgumentNotValidException` | `400 Bad Request` | Jakarta Bean Validation failure on request DTO |

Validation failures include a `details` map with per-field error messages:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "details": {
    "plateNumber": "Plate number is required",
    "capacity": "must be a positive number"
  }
}
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- MySQL 8+

### Database Setup

```sql
CREATE DATABASE fleetsync_db;
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fleetsync_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=<your_mysql_username>
spring.datasource.password=<your_mysql_password>

spring.jpa.hibernate.ddl-auto=update

# JWT (prepared for future use)
jwt.secret=<your_jwt_secret_minimum_32_chars>
jwt.expiration=86400000
```

> Set `ddl-auto=create` on first run to auto-generate tables, then switch to `update` for subsequent runs.

### Run the Application

```bash
# Clone the repository
git clone <repo-url>
cd FleetSync

# Build and run using the Maven Wrapper
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## DTO Reference

### VehicleRequestDto
```json
{
  "plateNumber": "ABC-1234",
  "type": "TRUCK",
  "model": "Toyota Hilux",
  "capacity": 5
}
```

### VehicleResponseDto
```json
{
  "id": 1,
  "plateNumber": "ABC-1234",
  "model": "Toyota Hilux",
  "capacity": 5,
  "status": "AVAILABLE"
}
```

### DriverRequestDto
```json
{
  "firstName": "Ahmed",
  "lastName": "Ali",
  "licenseNumber": "LIC-9988",
  "phoneNumber": "+92-300-1234567",
  "licenseExpiry": "2027-06-30",
  "userId": 3
}
```

### DriverResponseDto
```json
{
  "id": 2,
  "firstName": "Ahmed",
  "lastName": "Ali",
  "licenseNumber": "LIC-9988",
  "phoneNumber": "+92-300-1234567",
  "licenseExpiry": "2027-06-30",
  "isActive": true
}
```

### TripRequestDto
```json
{
  "origin": "Karachi",
  "destination": "Lahore",
  "vehicleId": 1,
  "driverId": 2,
  "status": "PENDING"
}
```

### TripResponseDto
```json
{
  "id": 10,
  "origin": "Karachi",
  "destination": "Lahore",
  "status": "IN_PROGRESS",
  "notes": "Handle with care",
  "driverId": 2,
  "vehicleId": 1,
  "createdById": 5,
  "createdAt": "2025-01-15T08:00:00",
  "startedAt": "2025-01-15T09:00:00",
  "completedAt": null
}
```

### UserRequestDto
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "MANAGER"
}
```

### UserResponseDto
```json
{
  "id": 5,
  "username": "john.doe",
  "email": "john@example.com",
  "role": "MANAGER",
  "isActive": true,
  "createdAt": "2025-01-10T12:00:00"
}
```

---

## Security

JWT authentication is **planned** for a future iteration. The `jwt.secret` and `jwt.expiration` properties are already wired into `application.properties` in preparation.

The intended model when implemented:
- Tokens will be issued on login and included in the `Authorization: Bearer <token>` header for protected routes.
- Token expiry is configurable via `jwt.expiration` (default: 86400000 ms = 24 hours).
- Three roles will control access: `ADMIN` (full access), `MANAGER` (trip & vehicle management), `DRIVER` (view assigned trips).

---

## Execution Guide Highlights

Key points from the `FleetSyncExecution_Guide.pdf`:

1. **Database First** — Create the `fleetsync_db` MySQL database before starting the application.
2. **DDL Strategy** — Use `ddl-auto=create` on first run to generate the schema, then switch to `update`.
3. **JWT Secret** — The secret key must be at least 32 characters long for HS256 signing (relevant once security is wired in).
4. **Port** — The application runs on port `8080` by default; change via `server.port` in `application.properties`.
5. **Lazy Loading** — All entity relationships use `FetchType.LAZY` to avoid N+1 query issues; use DTOs to project only required fields.
6. **Validation** — All request DTOs are validated with Jakarta Bean Validation; invalid requests return `400 Bad Request` with field-level error messages.
7. **Audit Fields** — `createdAt` and `updatedAt` are managed automatically by JPA `@PrePersist` and `@PreUpdate` hooks — do not set them manually.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

> Built with Spring Boot · MySQL · Lombok · Jakarta Validation
