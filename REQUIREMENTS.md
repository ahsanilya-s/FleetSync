# FleetSync — Requirements & User Stories

## Scenario

**FleetSync** is a fleet management platform for companies that operate a fleet of vehicles (e.g., delivery companies, logistics firms, transport agencies). Fleet managers need a centralized system to track vehicles, assign drivers, monitor trips, handle maintenance, and get AI-powered insights — all through a REST API.

---

## Actors

| Actor | Description |
|---|---|
| Fleet Manager | Admin who oversees the entire fleet |
| Driver | Assigned to vehicles and trips |
| System (AI) | OpenAI-powered assistant for insights and alerts |

---

## User Stories

### Vehicle Management
- As a fleet manager, I want to **add a new vehicle** so that it can be assigned to drivers and trips.
- As a fleet manager, I want to **view all vehicles** with their current status (available, on-trip, in-maintenance).
- As a fleet manager, I want to **update vehicle details** (e.g., license plate, model, fuel type).
- As a fleet manager, I want to **delete a vehicle** that is no longer in service.

### Driver Management
- As a fleet manager, I want to **register a new driver** with their license and contact info.
- As a fleet manager, I want to **view all drivers** and their current assignment status.
- As a fleet manager, I want to **assign a driver to a vehicle** for a specific trip.

### Trip Management
- As a fleet manager, I want to **create a trip** by specifying origin, destination, vehicle, and driver.
- As a fleet manager, I want to **track the status of a trip** (scheduled, in-progress, completed, cancelled).
- As a driver, I want to **mark a trip as started or completed** to update its status.
- As a fleet manager, I want to **view trip history** for any vehicle or driver.

### Maintenance
- As a fleet manager, I want to **log a maintenance record** for a vehicle (date, type, cost).
- As a fleet manager, I want to **view upcoming maintenance** based on mileage or schedule.
- As a fleet manager, I want to **receive alerts** when a vehicle is due for service.

### AI-Powered Insights (OpenAI)
- As a fleet manager, I want to **ask the AI assistant** questions like "Which vehicle has the highest fuel cost this month?"
- As a fleet manager, I want the AI to **summarize fleet performance** based on trip and maintenance data.
- As a fleet manager, I want the AI to **suggest optimal vehicle assignments** based on trip distance and vehicle condition.

---

## Key Requirements

### Functional
- CRUD operations for Vehicles, Drivers, Trips, and Maintenance records
- Trip status lifecycle: `SCHEDULED → IN_PROGRESS → COMPLETED / CANCELLED`
- A vehicle cannot be assigned to two active trips simultaneously
- A driver cannot be assigned to two trips at the same time
- Maintenance records must be linked to a specific vehicle

### Non-Functional
- REST API with JSON responses
- All data persisted in a relational database via JDBC
- AI chat endpoint powered by Spring AI + OpenAI
- Input validation on all POST/PUT requests
- Meaningful HTTP status codes (200, 201, 400, 404, 409, 500)

---

## Core Entities

```
Vehicle       { id, licensePlate, model, year, fuelType, status }
Driver        { id, name, licenseNumber, phone, status }
Trip          { id, vehicleId, driverId, origin, destination, startTime, endTime, status }
Maintenance   { id, vehicleId, date, type, description, cost }
```

---

## API Endpoint Overview

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/vehicles` | List all vehicles |
| POST | `/api/vehicles` | Add a new vehicle |
| PUT | `/api/vehicles/{id}` | Update a vehicle |
| DELETE | `/api/vehicles/{id}` | Remove a vehicle |
| GET | `/api/drivers` | List all drivers |
| POST | `/api/drivers` | Register a driver |
| POST | `/api/trips` | Create a trip |
| PUT | `/api/trips/{id}/status` | Update trip status |
| GET | `/api/trips` | View all trips |
| POST | `/api/maintenance` | Log maintenance |
| GET | `/api/maintenance/{vehicleId}` | Get maintenance history |
| POST | `/api/ai/chat` | Ask the AI assistant |
