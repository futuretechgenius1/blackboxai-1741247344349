package com.ems.util;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Custom JUnit 5 extensions for the EMS application tests
 */
public final class TestExtensions {

    private TestExtensions() {
        // Private constructor to prevent instantiation
    }

    /**
     * Extension for logging test execution
     */
    public static class LoggingExtension implements 
            BeforeAllCallback, 
            AfterAllCallback, 
            BeforeEachCallback, 
            AfterEachCallback, 
            BeforeTestExecutionCallback, 
            AfterTestExecutionCallback {

        private static final Logger logger = LoggerFactory.getLogger(LoggingExtension.class);

        @Override
        public void beforeAll(ExtensionContext context) {
            logger.info("Starting test class: {}", context.getDisplayName());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            logger.info("Completed test class: {}", context.getDisplayName());
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            logger.info("Starting test: {}", context.getDisplayName());
        }

        @Override
        public void afterEach(ExtensionContext context) {
            logger.info("Completed test: {}", context.getDisplayName());
        }

        @Override
        public void beforeTestExecution(ExtensionContext context) {
            getStore(context).put(context.getRequiredTestMethod(), Instant.now());
        }

        @Override
        public void afterTestExecution(ExtensionContext context) {
            Method testMethod = context.getRequiredTestMethod();
            Instant start = getStore(context).remove(testMethod, Instant.class);
            Duration duration = Duration.between(start, Instant.now());
            logger.info("Test '{}' took {} ms", testMethod.getName(), duration.toMillis());
        }

        private ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
        }
    }

    /**
     * Extension for performance monitoring
     */
    public static class PerformanceMonitoringExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
        private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringExtension.class);

        @Override
        public void beforeTestExecution(ExtensionContext context) {
            getStore(context).put("start", System.currentTimeMillis());
        }

        @Override
        public void afterTestExecution(ExtensionContext context) {
            long start = getStore(context).remove("start", long.class);
            long duration = System.currentTimeMillis() - start;
            
            TestMetrics.recordPerformanceMetric(
                context.getRequiredTestMethod().getName(),
                "execution_time",
                duration
            );

            logger.info("Performance - Test '{}' took {} ms", 
                context.getRequiredTestMethod().getName(), 
                duration);
        }

        private ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
        }
    }

    /**
     * Extension for test retry on failure
     */
    public static class RetryExtension implements TestExecutionExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(RetryExtension.class);
        private static final int MAX_RETRIES = 3;

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            int currentAttempt = getStore(context).get("currentAttempt", Integer.class, 1);

            if (currentAttempt < MAX_RETRIES) {
                logger.warn("Test '{}' failed (attempt {}), retrying...", 
                    context.getRequiredTestMethod().getName(), 
                    currentAttempt);
                
                getStore(context).put("currentAttempt", currentAttempt + 1);
                
                // Re-run the test
                context.getRequiredTestMethod().invoke(context.getRequiredTestInstance());
            } else {
                logger.error("Test '{}' failed after {} attempts", 
                    context.getRequiredTestMethod().getName(), 
                    MAX_RETRIES);
                throw throwable;
            }
        }

        private ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
        }
    }

    /**
     * Extension for test cleanup
     */
    public static class CleanupExtension implements AfterEachCallback {
        private static final Logger logger = LoggerFactory.getLogger(CleanupExtension.class);

        @Override
        public void afterEach(ExtensionContext context) {
            Optional.ofNullable(context.getTestInstance())
                .ifPresent(instance -> {
                    if (instance instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) instance).close();
                            logger.info("Cleanup completed for test: {}", 
                                context.getDisplayName());
                        } catch (Exception e) {
                            logger.error("Error during cleanup: {}", e.getMessage());
                        }
                    }
                });
        }
    }

    /**
     * Extension for test metrics collection
     */
    public static class MetricsExtension implements 
            BeforeAllCallback, 
            AfterAllCallback, 
            BeforeTestExecutionCallback, 
            AfterTestExecutionCallback {

        @Override
        public void beforeAll(ExtensionContext context) {
            TestMetrics.startSuite(context.getDisplayName());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            TestMetrics.endSuite(context.getDisplayName());
        }

        @Override
        public void beforeTestExecution(ExtensionContext context) {
            TestMetrics.startTest(context.getDisplayName());
        }

        @Override
        public void afterTestExecution(ExtensionContext context) {
            if (context.getExecutionException().isPresent()) {
                TestMetrics.recordTestFailure(
                    context.getDisplayName(),
                    context.getExecutionException().get()
                );
            } else {
                TestMetrics.recordTestSuccess(context.getDisplayName());
            }
        }
    }

    /**
     * Extension for Spring test context customization
     */
    public static class SpringTestContextExtension extends SpringExtension {
        private static final Logger logger = LoggerFactory.getLogger(SpringTestContextExtension.class);

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            logger.info("Initializing Spring test context for: {}", 
                context.getDisplayName());
            super.beforeAll(context);
        }

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            logger.info("Cleaning up Spring test context for: {}", 
                context.getDisplayName());
            super.afterAll(context);
        }
    }
}
