CREATE DATABASE IF NOT EXISTS college_procurement;
USE college_procurement;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department_id VARCHAR(50) NULL
);

-- 2. Inventory Table
CREATE TABLE IF NOT EXISTS inventory (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    sku VARCHAR(50) UNIQUE NOT NULL
);

-- 3. Vendors Table
CREATE TABLE IF NOT EXISTS vendors (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact VARCHAR(50) NULL,
    email VARCHAR(100) NULL,
    gst_number VARCHAR(20) NULL,
    rating DOUBLE NOT NULL DEFAULT 5.0
);

-- 4. Procurement Requests Table
CREATE TABLE IF NOT EXISTS procurement_requests (
    id VARCHAR(50) PRIMARY KEY,
    requester_id VARCHAR(50) NOT NULL,
    requester_name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    estimated_cost DOUBLE NULL,
    actual_cost DOUBLE NULL,
    purpose TEXT NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id)
);
