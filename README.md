# FleetSync 2.0 — Fleet Management System

> A production-ready **Spring Boot REST API** for managing vehicles, drivers, and trips in real time. Built with Java 17, Spring Boot 4, MySQL, JPA/Hibernate, JWT authentication, and Lombok.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [Enums Reference](#enums-reference)
- [API Endpoints](#api-endpoints)
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

**FleetSync 2.0** is a backend fleet management platform that enables organisations to track and coordinate their entire vehicle fleet from a single REST API. Managers can register vehicles, onboard drivers, schedule trips, and monitor trip lifecycle states — all secured behind role-based JWT authentication.

---

## Key Features

- **Vehicle Management** — Register, update, and retire vehicles with real-time status tracking (`AVAILABLE`, `ON_TRIP`, `MAINTENANCE`, `RETIRED`)
- **Driver Management** — Onboard drivers, link them to user accounts, and track license expiry dates
- **Trip Scheduling** — Create and manage trips with full lifecycle support (`PENDING` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`)
- **Role-Based Access Control** — Three roles: `ADMIN`, `MANAGER`, `DRIVER` with JWT-secured endpoints
- **Audit Timestamps** — Automatic `createdAt` / `updatedAt` tracking on every entity via JPA lifecycle hooks
- **Bean Validation** — Input validation on all DTOs using Jakarta Validation annotations
- **Lazy Loading** — Optimised database queries with `FetchType.LAZY` on all relationships

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8+ |
| Security | JWT (JSON Web Tokens) |
| Build Tool | Maven |
| Boilerplate Reduction | Lombok |
| Validation | Jakarta Bean Validation |
| Server | Embedded Tomcat (port 8080) |

---

## Project Structure

```
FleetSync2.0/
├── src/
│   └── main/
│       ├── java/com/fleetsync/fleetsync/
│       │   ├── Application.java          # Spring Boot entry point
│       │   ├── config/                   # Security & app configuration
│       │   ├── controller/               # REST controllers
│       │   │   └── TestController.java
│       │   ├── dto/                      # Request / Response DTOs
│       │   │   ├── TripRequestDto.java
│       │   │   ├── VehicleRequestDto.java
│       │   │   └── VehicleResponseDto.java
│       │   ├── entity/                   # JPA entities
│       │   │   ├── Driver.java
│       │   │   ├── Trip.java
│       │   │   ├── User.java
│       │   │   └── Vehicle.java
│       │   ├── enums/                    # Domain enumerations
│       │   │   ├── Role.java
│       │   │   ├── TripStatus.java
│       │   │   ├── VehicleStatus.java
│       │   │   └── VehicleType.java
│       │   ├── exception/                # Custom exception handlers
│       │   ├── repository/               # Spring Data JPA repositories
│       │   │   ├── DriverRepository.java
│       │   │   ├── TripRepository.java
│       │   │   ├── UserRepository.java
│       │   │   └── VehicleRepository.java
│       │   ├── security/                 # JWT filter & security config
│       │   └── service/                  # Business logic layer
│       └── resources/
│           └── application.properties
├── pom.xml
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

### Test Endpoints
| Method | URL | Description |
|---|---|---|
| GET | `/api/test/hello` | Health check — returns greeting |
| GET | `/api/test/sheikh` | Health check — returns greeting |

> Full CRUD endpoints for Vehicles, Drivers, Trips, and Users are implemented in the service and controller layers.

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
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

jwt.secret=<your_jwt_secret_minimum_32_chars>
jwt.expiration=86400000
```

> Set `ddl-auto=create` on first run to auto-generate tables, then switch to `update` for subsequent runs.

### Run the Application

```bash
# Clone the repository
git clone <repo-url>
cd FleetSync2.0

# Build and run
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

---

## Security

FleetSync 2.0 uses **JWT (JSON Web Token)** authentication:

- Tokens are issued on login and must be included in the `Authorization: Bearer <token>` header for protected routes.
- Token expiry is configurable via `jwt.expiration` (default: 86400000 ms = 24 hours).
- Three roles control access: `ADMIN` (full access), `MANAGER` (trip & vehicle management), `DRIVER` (view assigned trips).

---

## Execution Guide Highlights

Key points from the `FleetSyncExecution_Guide.pdf`:

1. **Database First** — Create the `fleetsync_db` MySQL database before starting the application.
2. **DDL Strategy** — Use `ddl-auto=create` on first run to generate the schema, then switch to `update`.
3. **JWT Secret** — The secret key must be at least 32 characters long for HS256 signing.
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

> Built with Spring Boot · MySQL · JWT · Lombok · Jakarta Validation
