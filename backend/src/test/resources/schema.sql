-- Drop tables if they exist
DROP TABLE IF EXISTS work_logs;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    hourly_rate DECIMAL(10,2),
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create work_logs table
CREATE TABLE work_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    hours_worked DECIMAL(5,2) NOT NULL,
    remarks TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_date UNIQUE (user_id, date)
);

-- Create indexes
CREATE INDEX idx_work_logs_user_id ON work_logs(user_id);
CREATE INDEX idx_work_logs_date ON work_logs(date);
CREATE INDEX idx_work_logs_status ON work_logs(status);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Create trigger for updated_at timestamp
CREATE TRIGGER users_update_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
    SET NEW.updated_at = CURRENT_TIMESTAMP;

CREATE TRIGGER work_logs_update_timestamp
    BEFORE UPDATE ON work_logs
    FOR EACH ROW
    SET NEW.updated_at = CURRENT_TIMESTAMP;

-- Comments
COMMENT ON TABLE users IS 'Stores user information including employees and administrators';
COMMENT ON TABLE work_logs IS 'Stores daily work logs for employees';

COMMENT ON COLUMN users.role IS 'User role: ROLE_ADMIN or ROLE_EMPLOYEE';
COMMENT ON COLUMN users.hourly_rate IS 'Employee''s hourly pay rate';
COMMENT ON COLUMN work_logs.status IS 'Work log status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN work_logs.hours_worked IS 'Number of hours worked (max 2 decimal places)';
