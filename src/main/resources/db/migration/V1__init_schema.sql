-- ==============================================================
-- Flyway Database Migration: V1__init_schema.sql
-- Car Rental Application (Task JV-BN-002)
-- ==============================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(25),
    address VARCHAR(255),
    drivers_license VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    model_year INT,
    color VARCHAR(30),
    license_plate VARCHAR(30) NOT NULL UNIQUE,
    fuel_type VARCHAR(20) NOT NULL,
    transmission VARCHAR(20) NOT NULL,
    seats INT,
    category VARCHAR(30) NOT NULL,
    price_per_day DOUBLE NOT NULL,
    price_per_hour DOUBLE,
    security_deposit DOUBLE DEFAULT 0.0,
    description TEXT,
    image_url VARCHAR(500),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL,
    rating DOUBLE DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    pickup_date DATE NOT NULL,
    return_date DATE NOT NULL,
    pickup_location VARCHAR(100) NOT NULL,
    return_location VARCHAR(100) NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    payment_id VARCHAR(100),
    amount DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    receipt_url VARCHAR(500),
    paid_at DATETIME,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    description TEXT,
    maintenance_date DATE NOT NULL,
    cost DOUBLE,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Helpful Indexes for query optimization
CREATE INDEX idx_bookings_vehicle_dates ON bookings (vehicle_id, pickup_date, return_date);
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_vehicles_status_cat ON vehicles (status, category);
