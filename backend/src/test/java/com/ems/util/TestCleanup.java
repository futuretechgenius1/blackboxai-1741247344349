package com.ems.util;

import com.ems.repository.UserRepository;
import com.ems.repository.WorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Utility class for cleaning up test data and managing database state
 */
@Component
public class TestCleanup {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkLogRepository workLogRepository;

    /**
     * Cleans up all test data from the database
     */
    @Transactional
    public void cleanupDatabase() {
        // Delete all work logs first due to foreign key constraints
        workLogRepository.deleteAll();
        
        // Delete all users
        userRepository.deleteAll();

        // Clear persistence context
        entityManager.clear();
    }

    /**
     * Cleans up specific tables
     */
    @Transactional
    public void cleanupTables(String... tableNames) {
        for (String tableName : tableNames) {
            jdbcTemplate.execute("DELETE FROM " + tableName);
        }
        entityManager.clear();
    }

    /**
     * Resets auto-increment sequences
     */
    @Transactional
    public void resetSequences() {
        List<String> sequences = List.of(
            "work_log_seq",
            "user_seq"
        );

        for (String sequence : sequences) {
            try {
                jdbcTemplate.execute("ALTER SEQUENCE " + sequence + " RESTART WITH 1");
            } catch (Exception e) {
                // Ignore if sequence doesn't exist
            }
        }
    }

    /**
     * Truncates all tables and resets sequences
     */
    @Transactional
    public void truncateAllTables() {
        List<String> tables = List.of(
            "work_logs",
            "users"
        );

        // Disable foreign key constraints
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Truncate all tables
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }

        // Re-enable foreign key constraints
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // Reset sequences
        resetSequences();

        // Clear persistence context
        entityManager.clear();
    }

    /**
     * Cleans up specific user's data
     */
    @Transactional
    public void cleanupUserData(Long userId) {
        // Delete user's work logs
        workLogRepository.deleteByUserId(userId);
        
        // Delete user
        userRepository.deleteById(userId);

        entityManager.clear();
    }

    /**
     * Cleans up work logs for a specific date range
     */
    @Transactional
    public void cleanupWorkLogs(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        workLogRepository.deleteByDateBetween(startDate, endDate);
        entityManager.clear();
    }

    /**
     * Cleans up test data older than specified days
     */
    @Transactional
    public void cleanupOldTestData(int days) {
        java.time.LocalDate cutoffDate = java.time.LocalDate.now().minusDays(days);
        workLogRepository.deleteByDateBefore(cutoffDate);
        entityManager.clear();
    }

    /**
     * Verifies database is clean
     */
    public boolean isDatabaseClean() {
        return workLogRepository.count() == 0 && userRepository.count() == 0;
    }

    /**
     * Cleans up and verifies
     */
    @Transactional
    public void cleanupAndVerify() {
        cleanupDatabase();
        if (!isDatabaseClean()) {
            throw new RuntimeException("Database cleanup failed");
        }
    }

    /**
     * Executes custom cleanup SQL
     */
    @Transactional
    public void executeCustomCleanup(String sql) {
        jdbcTemplate.execute(sql);
        entityManager.clear();
    }

    /**
     * Cleans up test files
     */
    public void cleanupTestFiles(String directory) {
        java.io.File dir = new java.io.File(directory);
        if (dir.exists() && dir.isDirectory()) {
            for (java.io.File file : dir.listFiles()) {
                file.delete();
            }
        }
    }

    /**
     * Resets test environment
     */
    @Transactional
    public void resetTestEnvironment() {
        // Clean database
        truncateAllTables();
        
        // Clean files
        cleanupTestFiles("test-files");
        
        // Reset any other test state
        entityManager.clear();
    }
}
