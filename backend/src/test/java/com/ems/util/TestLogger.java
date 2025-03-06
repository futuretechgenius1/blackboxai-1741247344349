package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for test logging and debugging
 */
public final class TestLogger {

    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AtomicInteger testCounter = new AtomicInteger(0);

    private TestLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Log the start of a test
     */
    public static void logTestStart(String testName) {
        int testNumber = testCounter.incrementAndGet();
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("\n========== TEST #{} START: {} - {} ==========", testNumber, testName, timestamp);
    }

    /**
     * Log the end of a test
     */
    public static void logTestEnd(String testName) {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("\n========== TEST END: {} - {} ==========\n", testName, timestamp);
    }

    /**
     * Log an HTTP request
     */
    public static void logRequest(String method, String url, Object body) {
        logger.info("\nHTTP Request:");
        logger.info("Method: {}", method);
        logger.info("URL: {}", url);
        if (body != null) {
            logger.info("Body: {}", JsonUtil.toJson(body));
        }
    }

    /**
     * Log an HTTP response
     */
    public static void logResponse(int status, String body) {
        logger.info("\nHTTP Response:");
        logger.info("Status: {}", status);
        if (body != null) {
            logger.info("Body: {}", body);
        }
    }

    /**
     * Create a ResultHandler to log MockMvc response
     */
    public static ResultHandler logMockResponse() {
        return result -> {
            logger.info("\nMockMvc Response:");
            logger.info("Status: {}", result.getResponse().getStatus());
            logger.info("Headers: {}", result.getResponse().getHeaderNames());
            logger.info("Body: {}", result.getResponse().getContentAsString());
        };
    }

    /**
     * Log a test step
     */
    public static void logStep(String step) {
        logger.info("\n----- STEP: {} -----", step);
    }

    /**
     * Log an assertion
     */
    public static void logAssertion(String assertion) {
        logger.info("Asserting: {}", assertion);
    }

    /**
     * Log an exception
     */
    public static void logException(String message, Throwable throwable) {
        logger.error("\nException occurred: {}", message);
        logger.error("Exception type: {}", throwable.getClass().getName());
        logger.error("Exception message: {}", throwable.getMessage());
        logger.error("Stack trace: {}", Arrays.toString(throwable.getStackTrace()));
    }

    /**
     * Log test data
     */
    public static void logTestData(String description, Object data) {
        logger.info("\nTest Data - {}:", description);
        logger.info(JsonUtil.toJson(data));
    }

    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String operation, String details) {
        logger.info("\nDatabase Operation - {}: {}", operation, details);
    }

    /**
     * Log authentication information
     */
    public static void logAuthentication(String username, String token) {
        logger.info("\nAuthentication Info:");
        logger.info("Username: {}", username);
        logger.info("Token: {}", token);
    }

    /**
     * Log performance metrics
     */
    public static void logPerformance(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        logger.info("\nPerformance - {}: {} ms", operation, duration);
    }

    /**
     * Log test configuration
     */
    public static void logTestConfig(String key, String value) {
        logger.info("Test Config - {}: {}", key, value);
    }

    /**
     * Log test cleanup
     */
    public static void logCleanup(String resource) {
        logger.info("Cleaning up: {}", resource);
    }

    /**
     * Log test warning
     */
    public static void logWarning(String message) {
        logger.warn("\nWARNING: {}", message);
    }

    /**
     * Log test debug information
     */
    public static void logDebug(String message) {
        logger.debug("\nDEBUG: {}", message);
    }

    /**
     * Log test error
     */
    public static void logError(String message) {
        logger.error("\nERROR: {}", message);
    }

    /**
     * Log test info
     */
    public static void logInfo(String message) {
        logger.info("\nINFO: {}", message);
    }

    /**
     * Log test trace
     */
    public static void logTrace(String message) {
        logger.trace("\nTRACE: {}", message);
    }

    /**
     * Log MockMvc result actions
     */
    public static void logResultActions(ResultActions resultActions) throws Exception {
        resultActions.andDo(logMockResponse());
    }
}
