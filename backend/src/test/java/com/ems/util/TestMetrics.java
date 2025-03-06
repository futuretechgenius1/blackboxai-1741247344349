package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for collecting and reporting test execution metrics
 */
public final class TestMetrics {

    private static final Logger logger = LoggerFactory.getLogger(TestMetrics.class);
    private static final Map<String, TestSuite> suites = new ConcurrentHashMap<>();
    private static final Map<String, Instant> testStartTimes = new ConcurrentHashMap<>();
    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger passedTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);
    private static final AtomicInteger skippedTests = new AtomicInteger(0);
    private static final List<TestResult> testResults = new ArrayList<>();
    private static Instant suiteStartTime;

    private TestMetrics() {
        // Private constructor to prevent instantiation
    }

    /**
     * Start test suite execution
     */
    public static void startSuite(String suiteName) {
        suiteStartTime = Instant.now();
        suites.put(suiteName, new TestSuite(suiteName));
        logger.info("Started test suite: {}", suiteName);
    }

    /**
     * Start individual test execution
     */
    public static void startTest(String testName) {
        testStartTimes.put(testName, Instant.now());
        totalTests.incrementAndGet();
        logger.debug("Started test: {}", testName);
    }

    /**
     * Record test success
     */
    public static void recordTestSuccess(String testName) {
        Instant startTime = testStartTimes.remove(testName);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            testResults.add(new TestResult(testName, TestStatus.PASSED, duration, null));
            passedTests.incrementAndGet();
            logger.debug("Test passed: {} ({}ms)", testName, duration.toMillis());
        }
    }

    /**
     * Record test failure
     */
    public static void recordTestFailure(String testName, Throwable error) {
        Instant startTime = testStartTimes.remove(testName);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            testResults.add(new TestResult(testName, TestStatus.FAILED, duration, error));
            failedTests.incrementAndGet();
            logger.error("Test failed: {} ({}ms) - {}", testName, duration.toMillis(), error.getMessage());
        }
    }

    /**
     * Record test skip
     */
    public static void recordTestSkipped(String testName, String reason) {
        testResults.add(new TestResult(testName, TestStatus.SKIPPED, Duration.ZERO, null));
        skippedTests.incrementAndGet();
        logger.warn("Test skipped: {} - {}", testName, reason);
    }

    /**
     * End test suite execution and generate report
     */
    public static TestReport endSuite(String suiteName) {
        Duration totalDuration = Duration.between(suiteStartTime, Instant.now());
        TestSuite suite = suites.remove(suiteName);
        
        if (suite != null) {
            TestReport report = new TestReport(
                suiteName,
                totalTests.get(),
                passedTests.get(),
                failedTests.get(),
                skippedTests.get(),
                totalDuration,
                new ArrayList<>(testResults)
            );

            // Log summary
            logger.info("\n=== Test Suite Report: {} ===", suiteName);
            logger.info("Total Duration: {}ms", totalDuration.toMillis());
            logger.info("Total Tests: {}", report.totalTests());
            logger.info("Passed: {}", report.passed());
            logger.info("Failed: {}", report.failed());
            logger.info("Skipped: {}", report.skipped());
            logger.info("Success Rate: {}%", report.getSuccessRate());

            // Reset counters
            resetMetrics();

            return report;
        }

        return null;
    }

    /**
     * Reset all metrics
     */
    public static void resetMetrics() {
        totalTests.set(0);
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
        testResults.clear();
        testStartTimes.clear();
    }

    /**
     * Get current success rate
     */
    public static double getSuccessRate() {
        int total = totalTests.get();
        return total > 0 ? (passedTests.get() * 100.0) / total : 0.0;
    }

    /**
     * Record test performance metric
     */
    public static void recordPerformanceMetric(String testName, String metricName, long value) {
        logger.info("Performance Metric - {}.{}: {}ms", testName, metricName, value);
    }

    // Inner classes for test metrics
    private enum TestStatus {
        PASSED, FAILED, SKIPPED
    }

    private static class TestSuite {
        private final String name;
        private final Instant startTime;
        private final Map<String, Object> metadata;

        TestSuite(String name) {
            this.name = name;
            this.startTime = Instant.now();
            this.metadata = new HashMap<>();
        }
    }

    private static class TestResult {
        private final String testName;
        private final TestStatus status;
        private final Duration duration;
        private final Throwable error;

        TestResult(String testName, TestStatus status, Duration duration, Throwable error) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.error = error;
        }
    }

    public static class TestReport {
        private final String suiteName;
        private final int totalTests;
        private final int passed;
        private final int failed;
        private final int skipped;
        private final Duration totalDuration;
        private final List<TestResult> results;

        TestReport(String suiteName, int totalTests, int passed, int failed, int skipped,
                  Duration totalDuration, List<TestResult> results) {
            this.suiteName = suiteName;
            this.totalTests = totalTests;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
            this.totalDuration = totalDuration;
            this.results = results;
        }

        public String suiteName() { return suiteName; }
        public int totalTests() { return totalTests; }
        public int passed() { return passed; }
        public int failed() { return failed; }
        public int skipped() { return skipped; }
        public Duration totalDuration() { return totalDuration; }
        public List<TestResult> results() { return results; }
        public double getSuccessRate() {
            return totalTests > 0 ? (passed * 100.0) / totalTests : 0.0;
        }
    }
}
