-- Clear existing data
DELETE FROM work_logs;
DELETE FROM users;

-- Insert test users
INSERT INTO users (
    id, username, email, password, first_name, last_name,
    role, department, position, hourly_rate, enabled,
    created_at, updated_at
) VALUES
-- Admin user (password: Admin@123)
(1, 'admin.test', 'admin.test@ems.com', 
 '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG',
 'Admin', 'Test', 'ROLE_ADMIN', 'Administration', 'System Administrator',
 50.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Employee 1 (password: Employee@123)
(2, 'john.doe', 'john.doe@ems.com',
 '$2a$10$DMEh7N1LQpuJwQhfXVeka.h6GvZGvEsQpzxwXqsjxW5tLVKuE4lFS',
 'John', 'Doe', 'ROLE_EMPLOYEE', 'Engineering', 'Software Engineer',
 35.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Employee 2 (password: Employee@123)
(3, 'jane.smith', 'jane.smith@ems.com',
 '$2a$10$DMEh7N1LQpuJwQhfXVeka.h6GvZGvEsQpzxwXqsjxW5tLVKuE4lFS',
 'Jane', 'Smith', 'ROLE_EMPLOYEE', 'Marketing', 'Marketing Specialist',
 30.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test work logs
INSERT INTO work_logs (
    id, user_id, date, hours_worked, remarks,
    status, created_at, updated_at
) VALUES
-- John Doe's work logs
(1, 2, CURRENT_DATE - 1, 8.0, 'Regular work day',
 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, CURRENT_DATE, 9.5, 'Extra time for project deadline',
 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Jane Smith's work logs
(3, 3, CURRENT_DATE - 1, 8.0, 'Regular work day',
 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, CURRENT_DATE, 8.0, 'Client meeting and documentation',
 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Reset sequence values
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE work_logs ALTER COLUMN id RESTART WITH 5;
