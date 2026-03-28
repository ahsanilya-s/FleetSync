CREATE TABLE IF NOT EXISTS vehicles (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    plate_number VARCHAR(20)  NOT NULL UNIQUE,
    capacity     INT          NOT NULL,
    fuel_type    VARCHAR(30)  NOT NULL DEFAULT 'UNKNOWN',
    status       VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'DRIVER',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Stores driver profiles registered by the fleet manager
-- A driver is a separate entity from a user account
CREATE TABLE IF NOT EXISTS drivers (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    license_number  VARCHAR(50)  NOT NULL UNIQUE,  -- Unique driving license ID
    phone           VARCHAR(20)  NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE or ON_TRIP
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Stores trip assignments linking a driver to a vehicle
-- Created by the fleet manager to assign a driver to a vehicle for a trip
CREATE TABLE IF NOT EXISTS trips (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    driver_id   BIGINT       NOT NULL,
    vehicle_id  BIGINT       NOT NULL,
    origin      VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- If driver or vehicle is deleted, the trip record is also removed
    FOREIGN KEY (driver_id)  REFERENCES drivers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
