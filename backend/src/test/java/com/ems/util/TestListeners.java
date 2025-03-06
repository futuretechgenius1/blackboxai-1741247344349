package com.ems.util;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom test execution listeners for the EMS application tests
 */
public final class TestListeners {

    private TestListeners() {
        // Private constructor to prevent instantiation
    }

    /**
     * JUnit Platform test execution listener
     */
    public static class JUnitExecutionListener implements TestExecutionListener {
        private static final Logger logger = LoggerFactory.getLogger(JUnitExecutionListener.class);
        private final Map<String, Instant> testStartTimes = new ConcurrentHashMap<>();

        @Override
        public void testPlanExecutionStarted(TestPlan testPlan) {
            logger.info("\n=== Test Plan Execution Started ===");
            logger.info("Total tests to execute: {}", testPlan.countTestIdentifiers(TestIdentifier::isTest));
        }

        @Override
        public void testPlanExecutionFinished(TestPlan testPlan) {
            logger.info("\n=== Test Plan Execution Finished ===");
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (testIdentifier.isTest()) {
                testStartTimes.put(testIdentifier.getUniqueId(), Instant.now());
                logger.info("\nTest Started: {}", testIdentifier.getDisplayName());
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (testIdentifier.isTest()) {
                Instant startTime = testStartTimes.remove(testIdentifier.getUniqueId());
                Duration duration = Duration.between(startTime, Instant.now());

                logger.info("Test Finished: {}", testIdentifier.getDisplayName());
                logger.info("Status: {}", testExecutionResult.getStatus());
                logger.info("Duration: {} ms", duration.toMillis());

                testExecutionResult.getThrowable().ifPresent(throwable ->
                    logger.error("Test failed with exception:", throwable)
                );
            }
        }

        @Override
        public void executionSkipped(TestIdentifier testIdentifier, String reason) {
            if (testIdentifier.isTest()) {
                logger.warn("Test Skipped: {}", testIdentifier.getDisplayName());
                logger.warn("Reason: {}", reason);
            }
        }
    }

    /**
     * Spring test execution listener
     */
    public static class SpringTestListener extends AbstractTestExecutionListener {
        private static final Logger logger = LoggerFactory.getLogger(SpringTestListener.class);
        private final Map<String, Object> testContextData = new HashMap<>();

        @Override
        public void beforeTestClass(TestContext testContext) {
            logger.info("\n=== Test Class Started: {} ===", 
                testContext.getTestClass().getSimpleName());
        }

        @Override
        public void beforeTestMethod(TestContext testContext) {
            logger.info("\nTest Method Started: {}", testContext.getTestMethod().getName());
            testContextData.put("startTime", Instant.now());
        }

        @Override
        public void beforeTestExecution(TestContext testContext) {
            // Store any test-specific data
            testContextData.put("testInstance", testContext.getTestInstance());
        }

        @Override
        public void afterTestExecution(TestContext testContext) {
            // Clean up test-specific data
            testContextData.remove("testInstance");
        }

        @Override
        public void afterTestMethod(TestContext testContext) {
            Instant startTime = (Instant) testContextData.remove("startTime");
            Duration duration = Duration.between(startTime, Instant.now());

            logger.info("Test Method Finished: {}", testContext.getTestMethod().getName());
            logger.info("Duration: {} ms", duration.toMillis());

            if (testContext.getTestException() != null) {
                logger.error("Test failed with exception:", testContext.getTestException());
            }
        }

        @Override
        public void afterTestClass(TestContext testContext) {
            logger.info("\n=== Test Class Finished: {} ===\n", 
                testContext.getTestClass().getSimpleName());
        }
    }

    /**
     * Performance monitoring listener
     */
    public static class PerformanceListener extends AbstractTestExecutionListener {
        private static final Logger logger = LoggerFactory.getLogger(PerformanceListener.class);
        private static final long SLOW_TEST_THRESHOLD = 1000; // 1 second

        @Override
        public void beforeTestMethod(TestContext testContext) {
            testContext.setAttribute("startTime", System.currentTimeMillis());
        }

        @Override
        public void afterTestMethod(TestContext testContext) {
            long startTime = (long) testContext.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;

            if (duration > SLOW_TEST_THRESHOLD) {
                logger.warn("Slow test detected: {} took {} ms", 
                    testContext.getTestMethod().getName(), 
                    duration);
            }

            TestMetrics.recordPerformanceMetric(
                testContext.getTestMethod().getName(),
                "execution_time",
                duration
            );
        }
    }

    /**
     * Test cleanup listener
     */
    public static class CleanupListener extends AbstractTestExecutionListener {
        private static final Logger logger = LoggerFactory.getLogger(CleanupListener.class);

        @Override
        public void afterTestMethod(TestContext testContext) {
            if (testContext.getTestInstance() instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) testContext.getTestInstance()).close();
                    logger.info("Cleanup completed for: {}", 
                        testContext.getTestMethod().getName());
                } catch (Exception e) {
                    logger.error("Error during cleanup: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Test metrics listener
     */
    public static class MetricsListener extends AbstractTestExecutionListener {
        @Override
        public void beforeTestClass(TestContext testContext) {
            TestMetrics.startSuite(testContext.getTestClass().getSimpleName());
        }

        @Override
        public void beforeTestMethod(TestContext testContext) {
            TestMetrics.startTest(testContext.getTestMethod().getName());
        }

        @Override
        public void afterTestMethod(TestContext testContext) {
            if (testContext.getTestException() != null) {
                TestMetrics.recordTestFailure(
                    testContext.getTestMethod().getName(),
                    testContext.getTestException()
                );
            } else {
                TestMetrics.recordTestSuccess(testContext.getTestMethod().getName());
            }
        }

        @Override
        public void afterTestClass(TestContext testContext) {
            TestMetrics.endSuite(testContext.getTestClass().getSimpleName());
        }
    }
}
