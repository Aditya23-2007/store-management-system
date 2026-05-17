-- Seed Demo Users
INSERT INTO users (id, email, role, name, department_id) VALUES 
('0', 'admin@college.edu', 'super_admin', 'System Admin', 'it')
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO users (id, email, role, name, department_id) VALUES 
('1', 'director@college.edu', 'director', 'Dr. Satish Sharma', 'administration')
ON DUPLICATE KEY UPDATE email=email;


INSERT INTO users (id, email, role, name, department_id) VALUES 
('2', 'accountant@college.edu', 'accountant', 'Mrs. Priya Patel', 'finance')
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO users (id, email, role, name, department_id) VALUES 
('3', 'manager@college.edu', 'store_manager', 'Mr. Ramesh Kumar', 'store')
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO users (id, email, role, name, department_id) VALUES 
('4', 'faculty@college.edu', 'faculty', 'Prof. Amit Verma', 'computer_science')
ON DUPLICATE KEY UPDATE email=email;


-- Seed Demo Inventory Items
INSERT INTO inventory (id, name, category, quantity, unit, low_stock_threshold, sku) VALUES 
('inv_1', 'A4 Paper Reams', 'Stationery', 4, 'reams', 10, 'ST-A4-PAP')
ON DUPLICATE KEY UPDATE sku=sku;

INSERT INTO inventory (id, name, category, quantity, unit, low_stock_threshold, sku) VALUES 
('inv_2', 'Projector Bulbs', 'Electronics', 15, 'units', 5, 'EL-PROJ-BLB')
ON DUPLICATE KEY UPDATE sku=sku;

INSERT INTO inventory (id, name, category, quantity, unit, low_stock_threshold, sku) VALUES 
('inv_3', 'Lab Notebooks', 'Stationery', 50, 'units', 20, 'ST-LAB-NB')
ON DUPLICATE KEY UPDATE sku=sku;


-- Seed Demo Vendors
INSERT INTO vendors (id, name, contact, email, gst_number, rating) VALUES 
('ven_1', 'Apex IT Solutions', '+91 98765 43210', 'sales@apextech.com', '07AAAAA1111A1Z1', 4.8)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO vendors (id, name, contact, email, gst_number, rating) VALUES 
('ven_2', 'Royal Stationery Mart', '+91 99887 76655', 'order@royalstationery.com', '07BBBBB2222B2Z2', 4.5)
ON DUPLICATE KEY UPDATE id=id;
