package com.ems.util;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for managing test database operations
 */
public final class TestDatabase {

    private static final Logger logger = LoggerFactory.getLogger(TestDatabase.class);
    private static final AtomicLong idGenerator = new AtomicLong(1);
    private static EmbeddedDatabase embeddedDatabase;
    private static JdbcTemplate jdbcTemplate;

    private TestDatabase() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initialize test database
     */
    public static void initializeDatabase() {
        if (embeddedDatabase == null) {
            embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("data.sql")
                .build();
            jdbcTemplate = new JdbcTemplate(embeddedDatabase);
            logger.info("Test database initialized");
        }
    }

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        if (embeddedDatabase == null) {
            initializeDatabase();
        }
        return embeddedDatabase.getConnection();
    }

    /**
     * Get data source
     */
    public static DataSource getDataSource() {
        if (embeddedDatabase == null) {
            initializeDatabase();
        }
        return embeddedDatabase;
    }

    /**
     * Clean up database
     */
    public static void cleanupDatabase() {
        if (embeddedDatabase != null) {
            embeddedDatabase.shutdown();
            embeddedDatabase = null;
            jdbcTemplate = null;
            logger.info("Test database cleaned up");
        }
    }

    /**
     * Reset database to initial state
     */
    @Transactional
    public static void resetDatabase() {
        if (jdbcTemplate != null) {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
            jdbcTemplate.execute("TRUNCATE TABLE work_logs");
            jdbcTemplate.execute("TRUNCATE TABLE users");
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
            resetSequences();
            logger.info("Database reset to initial state");
        }
    }

    /**
     * Reset database sequences
     */
    private static void resetSequences() {
        if (jdbcTemplate != null) {
            jdbcTemplate.execute("ALTER SEQUENCE user_seq RESTART WITH 1");
            jdbcTemplate.execute("ALTER SEQUENCE worklog_seq RESTART WITH 1");
        }
    }

    /**
     * Query methods
     */
    public static List<Map<String, Object>> query(String sql) {
        if (jdbcTemplate != null) {
            return jdbcTemplate.queryForList(sql);
        }
        return null;
    }

    public static int update(String sql, Object... args) {
        if (jdbcTemplate != null) {
            return jdbcTemplate.update(sql, args);
        }
        return 0;
    }

    /**
     * Create test data
     */
    @Transactional
    public static void createTestData() {
        if (jdbcTemplate == null) {
            initializeDatabase();
        }
        createTestUsers();
        createTestWorkLogs();
        logger.info("Test data created");
    }

    /**
     * Create test users
     */
    private static void createTestUsers() {
        String sql = "INSERT INTO users (id, username, email, password, first_name, last_name, " +
                    "role, department, position, hourly_rate, enabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Admin user
        jdbcTemplate.update(sql,
            idGenerator.getAndIncrement(),
            "admin.test",
            "admin.test@ems.com",
            TestSecurity.encodePassword("Admin@123"),
            "Admin",
            "Test",
            Role.ROLE_ADMIN.name(),
            "IT",
            "System Administrator",
            50.0,
            true
        );

        // Employee user
        jdbcTemplate.update(sql,
            idGenerator.getAndIncrement(),
            "employee.test",
            "employee.test@ems.com",
            TestSecurity.encodePassword("Employee@123"),
            "Employee",
            "Test",
            Role.ROLE_EMPLOYEE.name(),
            "IT",
            "Software Engineer",
            25.0,
            true
        );
    }

    /**
     * Create test work logs
     */
    private static void createTestWorkLogs() {
        String sql = "INSERT INTO work_logs (id, user_id, date, hours_worked, remarks, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        LocalDate today = LocalDate.now();
        
        // Create work logs for employee
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.update(sql,
                idGenerator.getAndIncrement(),
                2L, // employee user id
                today.minusDays(i),
                8.0,
                "Regular work day " + (i + 1),
                WorkLog.WorkLogStatus.PENDING.name()
            );
        }
    }

    /**
     * Verify database state
     */
    public static boolean verifyDatabaseState() {
        if (jdbcTemplate == null) {
            return false;
        }

        try {
            int userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            int workLogCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM work_logs", Integer.class);
            
            return userCount > 0 && workLogCount > 0;
        } catch (Exception e) {
            logger.error("Failed to verify database state", e);
            return false;
        }
    }
}
