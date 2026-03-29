# FleetSync — Backend Flow Analysis

A complete walkthrough of every request flow in the application, from HTTP call to database, based on the actual source code.

---

## Table of Contents

1. [Application Startup](#1-application-startup)
2. [Security Layer — How Every Request is Authenticated](#2-security-layer)
3. [User Registration](#3-user-registration)
4. [Login (HTTP Basic Auth)](#4-login-http-basic-auth)
5. [Vehicle Management](#5-vehicle-management)
6. [Driver Management](#6-driver-management)
7. [Trip Management](#7-trip-management)
8. [Maintenance Tracking](#8-maintenance-tracking)
9. [AI Chat](#9-ai-chat)
10. [Database Schema Summary](#10-database-schema-summary)
11. [Full Request Flow Diagram](#11-full-request-flow-diagram)

---

## 1. Application Startup

**File:** `FleetSyncApplication.java`

Spring Boot scans all packages under `com.fleetsync.fleetsync` and wires every `@RestController`, `@Service`, `@Repository`, and `@Configuration` bean automatically.

**File:** `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fleetsync
spring.datasource.username=root
spring.datasource.password=aaaa
spring.sql.init.mode=always          # runs schema.sql on every startup
spring.ai.openai.api-key=<your_key>  # required by Spring AI
```

**File:** `schema.sql` — runs automatically on startup and creates 5 tables if they don't exist:

```sql
CREATE TABLE IF NOT EXISTS users    ( id, username, email, password_hash, role, created_at )
CREATE TABLE IF NOT EXISTS vehicles ( id, name, type, plate_number, capacity, fuel_type, status, created_at )
CREATE TABLE IF NOT EXISTS drivers  ( id, full_name, license_number, phone, email, status, created_at )
CREATE TABLE IF NOT EXISTS trips    ( id, driver_id, vehicle_id, origin, destination, status, start_time, end_time, created_at )
CREATE TABLE IF NOT EXISTS maintenance ( id, vehicle_id, date, type, description, cost, next_service_date, next_service_mileage, created_at )
```

---

## 2. Security Layer

**File:** `config/SecurityConfig.java`

Every HTTP request (except `/api/auth/register`) passes through Spring Security before reaching any controller. Here is exactly what happens:

### How Authentication Works

The app uses **HTTP Basic Auth** — the client sends credentials as a Base64-encoded header on every request:

```
Authorization: Basic <base64(username:password)>
```

Spring Security intercepts this, decodes it, and calls `UserService.loadUserByUsername()` to fetch the user from the database. It then uses `BCryptPasswordEncoder` to verify the submitted password against the stored hash.

```java
// SecurityConfig.java — wires our UserService + BCrypt into Spring Security
DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
provider.setPasswordEncoder(passwordEncoder()); // BCrypt strength 12
```

### Authorization Rules (from SecurityConfig)

```java
.requestMatchers("/api/auth/register").permitAll()                          // No auth needed
.requestMatchers(HttpMethod.PUT, "/api/trips/**/status").hasAnyRole("MANAGER", "DRIVER") // Both roles
.requestMatchers("/api/vehicles/**", "/api/drivers/**", "/api/trips/**",
                 "/api/maintenance/**", "/api/ai/**").hasRole("MANAGER")    // MANAGER only
.anyRequest().authenticated()
```

| Endpoint | Who Can Access |
|---|---|
| `POST /api/auth/register` | Anyone (public) |
| `PUT /api/trips/{id}/status` | MANAGER or DRIVER |
| Everything else | MANAGER only |

### BCrypt Bean

```java
// Defined in SecurityConfig, used by UserService to hash passwords
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // strength 12 = ~300ms per hash
}
```

> **Circular dependency note:** `UserService` needs `BCryptPasswordEncoder`, and `SecurityConfig` needs `UserService`. This is solved with `@Lazy` on the `UserService` constructor parameter in `SecurityConfig`.

---

## 3. User Registration

**Endpoint:** `POST /api/auth/register`  
**Auth required:** No (public endpoint)

### Flow

```
Client
  │
  ▼
POST /api/auth/register
{ "username": "ahsan", "email": "ahsan@mail.com", "password": "secret123", "role": "MANAGER" }
  │
  ▼
UserController.register()          ← @Valid triggers bean validation first
  │
  ├─ @NotBlank, @Size(min=3,max=50), @Pattern on username
  ├─ @NotBlank, @Email on email
  ├─ @NotBlank, @Size(min=8,max=128) on password
  └─ @NotBlank on role
  │
  ▼
UserService.register()
  │
  ├─ repo.existsByEmail()     → SELECT COUNT(*) FROM users WHERE email = ?
  │   └─ if exists → throw IllegalArgumentException("Email already in use")
  │
  ├─ repo.existsByUsername()  → SELECT COUNT(*) FROM users WHERE username = ?
  │   └─ if exists → throw IllegalArgumentException("Username already taken")
  │
  ├─ role.toUpperCase() — must be "MANAGER" or "DRIVER"
  │   └─ if invalid → throw IllegalArgumentException("Role must be MANAGER or DRIVER")
  │
  └─ encoder.encode(req.password())  ← BCrypt hash (strength 12)
       │
       ▼
     repo.save(username, email, hashedPassword, role)
       → INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)
  │
  ▼
HTTP 201 Created (empty body)
```

### Key Code — UserService.register()

```java
public void register(RegisterRequest req) {
    if (repo.existsByEmail(req.email()))
        throw new IllegalArgumentException("Email already in use");

    if (repo.existsByUsername(req.username()))
        throw new IllegalArgumentException("Username already taken");

    String role = req.role().toUpperCase();
    if (!role.equals("MANAGER") && !role.equals("DRIVER"))
        throw new IllegalArgumentException("Role must be MANAGER or DRIVER");

    // BCrypt hash before storing — plain text password never touches the DB
    repo.save(req.username(), req.email(), encoder.encode(req.password()), role);
}
```

### Error Responses

| Scenario | HTTP Status |
|---|---|
| Validation fails (blank field, bad email, short password) | `400 Bad Request` |
| Email or username already exists | `409 Conflict` |
| Success | `201 Created` |

---

## 4. Login (HTTP Basic Auth)

**There is no `/login` endpoint.** Spring Security handles login transparently on every protected request.

### Flow

```
Client
  │
  ▼
GET /api/vehicles  (or any protected endpoint)
Authorization: Basic YWhoYW46c2VjcmV0MTIz   ← base64("ahsan:secret123")
  │
  ▼
Spring Security intercepts the request
  │
  ▼
DaoAuthenticationProvider.authenticate()
  │
  ▼
UserService.loadUserByUsername("ahsan")
  │
  ▼
UserRepository.findByUsername("ahsan")
  → SELECT * FROM users WHERE username = 'ahsan'
  │
  ├─ Not found → UsernameNotFoundException → HTTP 401 Unauthorized
  │
  └─ Found → returns User record
       │
       ▼
     BCryptPasswordEncoder.matches(submittedPassword, storedHash)
       ├─ No match → HTTP 401 Unauthorized
       └─ Match → SecurityContext populated with username + ROLE_MANAGER (or ROLE_DRIVER)
            │
            ▼
          Authorization check (hasRole / hasAnyRole from SecurityConfig)
            ├─ Role not allowed → HTTP 403 Forbidden
            └─ Allowed → request reaches the controller
```

### Key Code — UserService.loadUserByUsername()

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repo.findByUsername(username)
        .map(user -> new org.springframework.security.core.userdetails.User(
            user.username(),
            user.passwordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role())) // e.g. ROLE_MANAGER
        ))
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}
```

---

## 5. Vehicle Management

**Base path:** `/api/vehicles`  
**Auth required:** MANAGER role

### 5a. Add Vehicle — `POST /api/vehicles`

```
Client (MANAGER)
  │
  ▼
POST /api/vehicles
{ "name": "Van A", "type": "Mini Van", "plateNumber": "ABC-123", "capacity": 5, "fuelType": "DIESEL" }
  │
  ▼
VehicleController.add()
  │
  ├─ @Valid → validates VehicleRequest fields
  │
  ├─ repo.existsByPlateNumber(plateNumber)
  │   → SELECT COUNT(*) FROM vehicles WHERE plate_number = ?
  │   └─ if exists → throw IllegalArgumentException("Plate number already registered") → 409
  │
  └─ repo.save(req)
       → INSERT INTO vehicles (name, type, plate_number, capacity, fuel_type) VALUES (?, ?, ?, ?, ?)
       (status defaults to 'AVAILABLE', created_at defaults to NOW())
  │
  ▼
HTTP 201 Created
```

### 5b. List All Vehicles — `GET /api/vehicles`

```
VehicleController.getAll()
  │
  ▼
repo.findAll()
  → SELECT * FROM vehicles ORDER BY created_at DESC
  │
  ▼
Returns List<Vehicle> as JSON
```

### 5c. Update Vehicle — `PUT /api/vehicles/{id}`

```
VehicleController.update(id, req)
  │
  ├─ repo.existsById(id)         → SELECT COUNT(*) FROM vehicles WHERE id = ?
  │   └─ not found → 404
  │
  ├─ repo.existsByPlateNumber(plateNumber, id)
  │   → SELECT COUNT(*) FROM vehicles WHERE plate_number = ? AND id != ?
  │   └─ belongs to another vehicle → 409
  │
  └─ repo.update(id, req)
       → UPDATE vehicles SET name=?, type=?, plate_number=?, capacity=?, fuel_type=? WHERE id=?
  │
  ▼
HTTP 204 No Content
```

### 5d. Delete Vehicle — `DELETE /api/vehicles/{id}`

```
VehicleController.delete(id)
  │
  ├─ repo.existsById(id) → 404 if not found
  │
  └─ repo.deleteById(id)
       → DELETE FROM vehicles WHERE id = ?
       (CASCADE deletes related trips and maintenance records via FK constraints)
  │
  ▼
HTTP 204 No Content
```

---

## 6. Driver Management

**Base path:** `/api/drivers`  
**Auth required:** MANAGER role

### 6a. Register Driver — `POST /api/drivers`

```
Client (MANAGER)
  │
  ▼
POST /api/drivers
{ "fullName": "Ali Khan", "licenseNumber": "LIC-001", "phone": "03001234567", "email": "ali@mail.com" }
  │
  ▼
DriverController.register()
  │
  ├─ @Valid → validates DriverRequest fields
  │
  ├─ repo.existsByLicense(licenseNumber)
  │   → SELECT COUNT(*) FROM drivers WHERE license_number = ?
  │   └─ exists → 409 "License number already registered"
  │
  ├─ repo.existsByEmail(email)
  │   → SELECT COUNT(*) FROM drivers WHERE email = ?
  │   └─ exists → 409 "Email already registered"
  │
  └─ repo.save(req)
       → INSERT INTO drivers (full_name, license_number, phone, email) VALUES (?, ?, ?, ?)
       (status defaults to 'AVAILABLE')
  │
  ▼
HTTP 201 Created
```

### 6b. List All Drivers — `GET /api/drivers`

```
DriverController.getAll()
  │
  ▼
repo.findAll()
  → SELECT * FROM drivers ORDER BY created_at DESC
  │
  ▼
Returns List<Driver> as JSON — each driver shows current status (AVAILABLE or ON_TRIP)
```

> **Note:** A driver entity is separate from a user account. A driver profile is managed by the fleet manager. A user account is for API authentication.

---

## 7. Trip Management

**Base path:** `/api/trips`  
**Auth required:** MANAGER (create/list), MANAGER or DRIVER (status update)

### 7a. Assign Trip — `POST /api/trips`

This is the most complex flow — it validates both driver and vehicle, checks for conflicts, saves the trip, and updates statuses atomically.

```
Client (MANAGER)
  │
  ▼
POST /api/trips
{ "driverId": 1, "vehicleId": 2, "origin": "Lahore", "destination": "Karachi" }
  │
  ▼
TripController.assign()
  │
  ├─ @Valid → validates TripRequest (@NotNull on IDs, @NotBlank on origin/destination)
  │
  ├─ driverRepo.existsById(driverId)
  │   → SELECT COUNT(*) FROM drivers WHERE id = ?
  │   └─ not found → 404 "Driver not found"
  │
  ├─ vehicleRepo.existsById(vehicleId)
  │   → SELECT COUNT(*) FROM vehicles WHERE id = ?
  │   └─ not found → 404 "Vehicle not found"
  │
  ├─ tripRepo.driverHasActiveTrip(driverId)
  │   → SELECT COUNT(*) FROM trips WHERE driver_id = ? AND status IN ('SCHEDULED','IN_PROGRESS')
  │   └─ active trip exists → 409 "Driver is already on an active trip"
  │
  ├─ tripRepo.vehicleHasActiveTrip(vehicleId)
  │   → SELECT COUNT(*) FROM trips WHERE vehicle_id = ? AND status IN ('SCHEDULED','IN_PROGRESS')
  │   └─ active trip exists → 409 "Vehicle is already on an active trip"
  │
  ├─ tripRepo.save(req)
  │   → INSERT INTO trips (driver_id, vehicle_id, origin, destination) VALUES (?, ?, ?, ?)
  │   (status defaults to 'SCHEDULED')
  │
  ├─ driverRepo.updateStatus(driverId, "ON_TRIP")
  │   → UPDATE drivers SET status = 'ON_TRIP' WHERE id = ?
  │
  └─ vehicleRepo.updateStatus(vehicleId, "ON_TRIP")
       → UPDATE vehicles SET status = 'ON_TRIP' WHERE id = ?
  │
  ▼
HTTP 201 Created
```

### 7b. Update Trip Status — `PUT /api/trips/{id}/status`

The trip follows a strict lifecycle. Invalid transitions are rejected.

```
Valid transitions:
  SCHEDULED   → IN_PROGRESS  (trip starts)
  SCHEDULED   → CANCELLED    (trip cancelled before starting)
  IN_PROGRESS → COMPLETED    (trip finished)
  IN_PROGRESS → CANCELLED    (trip cancelled mid-way)
  COMPLETED   → (nothing)    terminal
  CANCELLED   → (nothing)    terminal
```

```
Client (MANAGER or DRIVER)
  │
  ▼
PUT /api/trips/1/status
{ "status": "IN_PROGRESS" }
  │
  ▼
TripController.updateStatus(id, req)
  │
  ├─ tripRepo.findById(id)
  │   → SELECT * FROM trips WHERE id = ?
  │   └─ not found → 404 "Trip not found"
  │
  ├─ Validate next status is one of: IN_PROGRESS, COMPLETED, CANCELLED
  │   └─ invalid → 400 "Invalid status: ..."
  │
  ├─ Switch on current status to validate transition
  │   └─ invalid transition → 400 "Cannot transition trip from X to Y"
  │
  ├─ Set startTime = NOW() if moving to IN_PROGRESS, else null
  ├─ Set endTime   = NOW() if moving to COMPLETED or CANCELLED, else null
  │
  ├─ tripRepo.updateStatus(id, next, startTime, endTime)
  │   → UPDATE trips SET status=?, start_time=COALESCE(?,start_time), end_time=COALESCE(?,end_time) WHERE id=?
  │
  └─ If terminal (COMPLETED or CANCELLED):
       ├─ driverRepo.updateStatus(trip.driverId(), "AVAILABLE")
       │   → UPDATE drivers SET status = 'AVAILABLE' WHERE id = ?
       └─ vehicleRepo.updateStatus(trip.vehicleId(), "AVAILABLE")
            → UPDATE vehicles SET status = 'AVAILABLE' WHERE id = ?
  │
  ▼
HTTP 200 OK
```

### 7c. List Trips — `GET /api/trips`

Supports optional filtering:

```
GET /api/trips                        → all trips
GET /api/trips?driverId=1             → trips for driver 1
GET /api/trips?vehicleId=2            → trips for vehicle 2
```

```java
// TripController.getAll()
if (driverId != null) return tripRepo.findAllByDriverId(driverId);
if (vehicleId != null) return tripRepo.findAllByVehicleId(vehicleId);
return tripRepo.findAll();
```

---

## 8. Maintenance Tracking

**Base path:** `/api/maintenance`  
**Auth required:** MANAGER role

### 8a. Log Maintenance — `POST /api/maintenance`

```
Client (MANAGER)
  │
  ▼
POST /api/maintenance
{
  "vehicleId": 2,
  "date": "2025-04-01",
  "type": "OIL_CHANGE",
  "description": "Changed engine oil",
  "cost": 2500.00,
  "nextServiceDate": "2025-07-01",
  "nextServiceMileage": 15000
}
  │
  ▼
MaintenanceController.log()
  │
  ├─ @Valid → validates MaintenanceRequest
  │   ├─ @NotNull vehicleId, date, cost
  │   ├─ @NotBlank type
  │   ├─ @DecimalMin("0.0") cost
  │   └─ @Min(0) nextServiceMileage
  │
  ├─ vehicleRepo.existsById(vehicleId)
  │   → SELECT COUNT(*) FROM vehicles WHERE id = ?
  │   └─ not found → 404 "Vehicle not found"
  │
  └─ maintenanceRepo.save(req)
       → INSERT INTO maintenance (vehicle_id, date, type, description, cost,
                                  next_service_date, next_service_mileage)
         VALUES (?, ?, ?, ?, ?, ?, ?)
  │
  ▼
HTTP 201 Created
```

### 8b. Vehicle Maintenance History — `GET /api/maintenance/{vehicleId}`

```
MaintenanceController.history(vehicleId)
  │
  ├─ vehicleRepo.existsById(vehicleId) → 404 if not found
  │
  └─ maintenanceRepo.findByVehicleId(vehicleId)
       → SELECT * FROM maintenance WHERE vehicle_id = ? ORDER BY date DESC, created_at DESC
  │
  ▼
Returns List<Maintenance> as JSON
```

### 8c. Upcoming Maintenance — `GET /api/maintenance/upcoming`

```
GET /api/maintenance/upcoming?days=30&currentMileage=12000
  │
  ▼
MaintenanceController.upcoming(days, currentMileage)
  │
  └─ maintenanceRepo.findUpcoming(30, 12000)
       → SELECT * FROM maintenance
         WHERE (next_service_date IS NOT NULL AND next_service_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY))
            OR (next_service_mileage IS NOT NULL AND next_service_mileage <= 12000)
         ORDER BY next_service_date ASC
```

If `currentMileage` is omitted, only date-based filtering is applied.

### 8d. Maintenance Alerts — `GET /api/maintenance/alerts`

```
GET /api/maintenance/alerts?days=7
  │
  ▼
maintenanceRepo.findAlerts(7)
  → SELECT * FROM maintenance
    WHERE next_service_date IS NOT NULL
      AND next_service_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
    ORDER BY next_service_date ASC
```

Returns vehicles overdue or due within 7 days — intended for dashboard alerts.

---

## 9. AI Chat

**Endpoint:** `POST /api/ai/chat`  
**Auth required:** MANAGER role

This is the most interesting flow — it combines live database data with OpenAI to answer natural-language questions.

```
Client (MANAGER)
  │
  ▼
POST /api/ai/chat
{ "message": "Which vehicle has the highest maintenance cost this month?" }
  │
  ▼
AiController.chat()
  │
  ▼
AiService.chat(question)
  │
  ├─ buildFleetContext()  ← queries ALL 4 tables to build a text snapshot
  │   │
  │   ├─ vehicleRepo.findAll()
  │   │   → SELECT * FROM vehicles ORDER BY created_at DESC
  │   │
  │   ├─ driverRepo.findAll()
  │   │   → SELECT * FROM drivers ORDER BY created_at DESC
  │   │
  │   ├─ tripRepo.findAll()
  │   │   → SELECT * FROM trips ORDER BY created_at DESC
  │   │   (capped at 200 most recent to stay within OpenAI token limits)
  │   │
  │   └─ maintenanceRepo.findAll()
  │       → SELECT * FROM maintenance ORDER BY date DESC, created_at DESC LIMIT 200
  │
  ├─ Builds system prompt:
  │   "You are FleetSync AI... Fleet Data: [vehicles, drivers, trips, maintenance]"
  │
  └─ chatClient.prompt()
       .system(systemMessage)   ← fleet context injected here
       .user(question)          ← manager's question
       .call()
       .content()               ← OpenAI API call via Spring AI
  │
  ▼
AiController returns AiResponse { "reply": "Vehicle ID=2 (Van B) has the highest..." }
  │
  ▼
HTTP 200 OK
```

### System Prompt Structure

```
You are FleetSync AI, an intelligent assistant for fleet managers.
You have access to the current fleet data below...

Fleet Data:
=== Vehicles (3) ===
ID=1, Name=Van A, Type=Mini Van, Plate=ABC-123, FuelType=DIESEL, Status=AVAILABLE
ID=2, Name=Van B, Type=Cargo Van, Plate=XYZ-456, FuelType=PETROL, Status=ON_TRIP
...

=== Drivers (2) ===
ID=1, Name=Ali Khan, Status=ON_TRIP
...

=== Trips (showing 5 of 5) ===
ID=1, VehicleID=2, DriverID=1, From=Lahore, To=Karachi, Status=IN_PROGRESS, CreatedAt=...
...

=== Maintenance Records (4) ===
ID=1, VehicleID=1, Date=2025-04-01, Type=OIL_CHANGE, Cost=2500.00
...
```

### Error Handling

```java
// AiController — catches any OpenAI API failure
@ExceptionHandler(Exception.class)
public ProblemDetail handleAiError(Exception ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    problem.setDetail("AI service is temporarily unavailable. Please try again later.");
    return problem;
}
```

---

## 10. Database Schema Summary

```
users
  id | username | email | password_hash | role (MANAGER/DRIVER) | created_at

vehicles
  id | name | type | plate_number | capacity | fuel_type | status (AVAILABLE/ON_TRIP) | created_at

drivers
  id | full_name | license_number | phone | email | status (AVAILABLE/ON_TRIP) | created_at

trips
  id | driver_id (FK→drivers) | vehicle_id (FK→vehicles) | origin | destination
     | status (SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED) | start_time | end_time | created_at

maintenance
  id | vehicle_id (FK→vehicles) | date | type | description | cost
     | next_service_date | next_service_mileage | created_at
```

**Foreign Key Cascades:**
- Deleting a vehicle → deletes all its trips and maintenance records
- Deleting a driver → deletes all their trips

---

## 11. Full Request Flow Diagram

```
                        ┌─────────────────────────────────────────────┐
                        │              HTTP Request                    │
                        └──────────────────┬──────────────────────────┘
                                           │
                        ┌──────────────────▼──────────────────────────┐
                        │         Spring Security Filter               │
                        │                                              │
                        │  /api/auth/register → permitAll()            │
                        │  All others → decode Basic Auth header       │
                        │    → UserService.loadUserByUsername()        │
                        │    → BCrypt.matches(password, hash)          │
                        │    → check hasRole / hasAnyRole              │
                        └──────────────────┬──────────────────────────┘
                                           │ authenticated + authorized
                        ┌──────────────────▼──────────────────────────┐
                        │              Controller Layer                │
                        │                                              │
                        │  UserController     → /api/auth/**           │
                        │  VehicleController  → /api/vehicles/**       │
                        │  DriverController   → /api/drivers/**        │
                        │  TripController     → /api/trips/**          │
                        │  MaintenanceController → /api/maintenance/** │
                        │  AiController       → /api/ai/**             │
                        └──────────────────┬──────────────────────────┘
                                           │ @Valid bean validation
                        ┌──────────────────▼──────────────────────────┐
                        │           Service Layer                      │
                        │                                              │
                        │  UserService  — registration + auth logic    │
                        │  AiService    — fleet context + OpenAI call  │
                        │  (other modules use repositories directly)   │
                        └──────────────────┬──────────────────────────┘
                                           │
                        ┌──────────────────▼──────────────────────────┐
                        │           Repository Layer (JdbcClient)      │
                        │                                              │
                        │  UserRepository        → users table         │
                        │  VehicleRepository     → vehicles table      │
                        │  DriverRepository      → drivers table       │
                        │  TripRepository        → trips table         │
                        │  MaintenanceRepository → maintenance table   │
                        └──────────────────┬──────────────────────────┘
                                           │
                        ┌──────────────────▼──────────────────────────┐
                        │              MySQL Database                  │
                        │         (fleetsync schema, port 3306)        │
                        └─────────────────────────────────────────────┘
```

---

## Quick Reference — All Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `GET` | `/api/vehicles` | MANAGER | List all vehicles |
| `POST` | `/api/vehicles` | MANAGER | Add a vehicle |
| `PUT` | `/api/vehicles/{id}` | MANAGER | Update a vehicle |
| `DELETE` | `/api/vehicles/{id}` | MANAGER | Delete a vehicle |
| `GET` | `/api/drivers` | MANAGER | List all drivers |
| `POST` | `/api/drivers` | MANAGER | Register a driver |
| `GET` | `/api/trips` | MANAGER | List trips (filter by driverId/vehicleId) |
| `POST` | `/api/trips` | MANAGER | Assign a trip |
| `PUT` | `/api/trips/{id}/status` | MANAGER or DRIVER | Update trip status |
| `POST` | `/api/maintenance` | MANAGER | Log maintenance record |
| `GET` | `/api/maintenance/{vehicleId}` | MANAGER | Vehicle maintenance history |
| `GET` | `/api/maintenance/upcoming` | MANAGER | Upcoming maintenance |
| `GET` | `/api/maintenance/alerts` | MANAGER | Overdue/due-soon alerts |
| `POST` | `/api/ai/chat` | MANAGER | AI-powered fleet insights |
