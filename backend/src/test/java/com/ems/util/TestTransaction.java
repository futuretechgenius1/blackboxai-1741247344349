package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Utility class for managing test database transactions
 */
public final class TestTransaction {

    private static final Logger logger = LoggerFactory.getLogger(TestTransaction.class);
    private static final ConcurrentHashMap<String, TransactionStatus> activeTransactions = new ConcurrentHashMap<>();
    private static final AtomicInteger transactionCounter = new AtomicInteger(0);
    private static PlatformTransactionManager transactionManager;
    private static TransactionTemplate transactionTemplate;

    private TestTransaction() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initialize transaction manager
     */
    public static void initialize(DataSource dataSource) {
        transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        logger.info("Transaction manager initialized");
    }

    /**
     * Begin a new transaction
     */
    public static String beginTransaction() {
        String transactionId = generateTransactionId();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(transactionId);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(def);
        activeTransactions.put(transactionId, status);
        logger.debug("Started transaction {}", transactionId);
        return transactionId;
    }

    /**
     * Commit a transaction
     */
    public static void commitTransaction(String transactionId) {
        TransactionStatus status = activeTransactions.remove(transactionId);
        if (status != null) {
            transactionManager.commit(status);
            logger.debug("Committed transaction {}", transactionId);
        } else {
            logger.warn("No active transaction found with ID {}", transactionId);
        }
    }

    /**
     * Rollback a transaction
     */
    public static void rollbackTransaction(String transactionId) {
        TransactionStatus status = activeTransactions.remove(transactionId);
        if (status != null) {
            transactionManager.rollback(status);
            logger.debug("Rolled back transaction {}", transactionId);
        } else {
            logger.warn("No active transaction found with ID {}", transactionId);
        }
    }

    /**
     * Execute in transaction
     */
    public static <T> T executeInTransaction(TransactionCallback<T> action) {
        return transactionTemplate.execute(action);
    }

    /**
     * Execute with new transaction
     */
    public static <T> T executeWithNewTransaction(Supplier<T> action) {
        String transactionId = beginTransaction();
        try {
            T result = action.get();
            commitTransaction(transactionId);
            return result;
        } catch (Exception e) {
            rollbackTransaction(transactionId);
            throw e;
        }
    }

    /**
     * Execute with transaction timeout
     */
    public static <T> T executeWithTimeout(Supplier<T> action, Duration timeout) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setTimeout((int) timeout.toSeconds());
        
        TransactionTemplate template = new TransactionTemplate(transactionManager, def);
        return template.execute(status -> action.get());
    }

    /**
     * Check if transaction is active
     */
    public static boolean isTransactionActive(String transactionId) {
        return activeTransactions.containsKey(transactionId);
    }

    /**
     * Get active transaction count
     */
    public static int getActiveTransactionCount() {
        return activeTransactions.size();
    }

    /**
     * Clear all transactions
     */
    public static void clearTransactions() {
        activeTransactions.forEach((id, status) -> {
            try {
                transactionManager.rollback(status);
                logger.debug("Rolled back transaction {} during cleanup", id);
            } catch (Exception e) {
                logger.error("Error rolling back transaction {} during cleanup", id, e);
            }
        });
        activeTransactions.clear();
    }

    /**
     * Monitor transaction performance
     */
    public static <T> T monitorTransactionPerformance(String transactionId, Supplier<T> action) {
        Instant start = Instant.now();
        try {
            T result = action.get();
            Duration duration = Duration.between(start, Instant.now());
            logger.info("Transaction {} completed in {} ms", transactionId, duration.toMillis());
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            logger.error("Transaction {} failed after {} ms: {}", 
                transactionId, duration.toMillis(), e.getMessage());
            throw e;
        }
    }

    /**
     * Generate transaction ID
     */
    private static String generateTransactionId() {
        return "TX-" + transactionCounter.incrementAndGet();
    }

    /**
     * Transaction result class
     */
    public static class TransactionResult<T> {
        private final T result;
        private final Duration duration;
        private final boolean successful;
        private final Exception error;

        private TransactionResult(T result, Duration duration, boolean successful, Exception error) {
            this.result = result;
            this.duration = duration;
            this.successful = successful;
            this.error = error;
        }

        public static <T> TransactionResult<T> success(T result, Duration duration) {
            return new TransactionResult<>(result, duration, true, null);
        }

        public static <T> TransactionResult<T> failure(Exception error, Duration duration) {
            return new TransactionResult<>(null, duration, false, error);
        }

        public T getResult() { return result; }
        public Duration getDuration() { return duration; }
        public boolean isSuccessful() { return successful; }
        public Exception getError() { return error; }
    }
}
