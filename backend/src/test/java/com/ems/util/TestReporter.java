package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating test execution reports
 */
public final class TestReporter {

    private static final Logger logger = LoggerFactory.getLogger(TestReporter.class);
    private static final String REPORTS_DIR = "test-reports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Map<String, TestSuite> suites = new ConcurrentHashMap<>();
    private static final AtomicInteger totalTests = new AtomicInteger(0);

    private TestReporter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Start recording a test suite
     */
    public static void startSuite(String suiteName) {
        suites.put(suiteName, new TestSuite(suiteName));
        logger.info("Started recording test suite: {}", suiteName);
    }

    /**
     * Record a test result
     */
    public static void recordTest(String suiteName, TestResult result) {
        TestSuite suite = suites.get(suiteName);
        if (suite != null) {
            suite.addResult(result);
            totalTests.incrementAndGet();
            logger.debug("Recorded test result for: {}", result.getTestName());
        }
    }

    /**
     * Generate HTML report for a test suite
     */
    public static void generateHtmlReport(String suiteName) {
        TestSuite suite = suites.get(suiteName);
        if (suite != null) {
            try {
                String reportContent = generateHtmlContent(suite);
                writeReport(suiteName, reportContent, "html");
                logger.info("Generated HTML report for suite: {}", suiteName);
            } catch (IOException e) {
                logger.error("Failed to generate HTML report for suite: {}", suiteName, e);
            }
        }
    }

    /**
     * Generate JSON report for a test suite
     */
    public static void generateJsonReport(String suiteName) {
        TestSuite suite = suites.get(suiteName);
        if (suite != null) {
            try {
                String reportContent = generateJsonContent(suite);
                writeReport(suiteName, reportContent, "json");
                logger.info("Generated JSON report for suite: {}", suiteName);
            } catch (IOException e) {
                logger.error("Failed to generate JSON report for suite: {}", suiteName, e);
            }
        }
    }

    /**
     * Generate summary report for all test suites
     */
    public static void generateSummaryReport() {
        try {
            String summaryContent = generateSummaryContent();
            writeReport("summary", summaryContent, "html");
            logger.info("Generated summary report for all test suites");
        } catch (IOException e) {
            logger.error("Failed to generate summary report", e);
        }
    }

    private static String generateHtmlContent(TestSuite suite) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n<head>\n")
            .append("<title>Test Report - ").append(suite.getName()).append("</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append(".passed { color: green; }\n")
            .append(".failed { color: red; }\n")
            .append(".skipped { color: orange; }\n")
            .append("table { border-collapse: collapse; width: 100%; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("th { background-color: #f2f2f2; }\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>Test Report - ").append(suite.getName()).append("</h1>\n")
            .append("<h2>Summary</h2>\n")
            .append("<p>Total Tests: ").append(suite.getTotalTests()).append("</p>\n")
            .append("<p>Passed: ").append(suite.getPassedTests()).append("</p>\n")
            .append("<p>Failed: ").append(suite.getFailedTests()).append("</p>\n")
            .append("<p>Skipped: ").append(suite.getSkippedTests()).append("</p>\n")
            .append("<p>Duration: ").append(suite.getDuration().toMillis()).append("ms</p>\n")
            .append("<h2>Test Results</h2>\n")
            .append("<table>\n")
            .append("<tr><th>Test</th><th>Status</th><th>Duration</th><th>Message</th></tr>\n");

        for (TestResult result : suite.getResults()) {
            html.append("<tr class=\"").append(result.getStatus().toLowerCase()).append("\">\n")
                .append("<td>").append(result.getTestName()).append("</td>\n")
                .append("<td>").append(result.getStatus()).append("</td>\n")
                .append("<td>").append(result.getDuration().toMillis()).append("ms</td>\n")
                .append("<td>").append(result.getMessage() != null ? result.getMessage() : "").append("</td>\n")
                .append("</tr>\n");
        }

        html.append("</table>\n</body>\n</html>");
        return html.toString();
    }

    private static String generateJsonContent(TestSuite suite) {
        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"suite\": \"").append(suite.getName()).append("\",\n")
            .append("  \"timestamp\": \"").append(LocalDateTime.now()).append("\",\n")
            .append("  \"summary\": {\n")
            .append("    \"total\": ").append(suite.getTotalTests()).append(",\n")
            .append("    \"passed\": ").append(suite.getPassedTests()).append(",\n")
            .append("    \"failed\": ").append(suite.getFailedTests()).append(",\n")
            .append("    \"skipped\": ").append(suite.getSkippedTests()).append(",\n")
            .append("    \"duration\": ").append(suite.getDuration().toMillis()).append("\n")
            .append("  },\n")
            .append("  \"results\": [\n");

        Iterator<TestResult> it = suite.getResults().iterator();
        while (it.hasNext()) {
            TestResult result = it.next();
            json.append("    {\n")
                .append("      \"name\": \"").append(result.getTestName()).append("\",\n")
                .append("      \"status\": \"").append(result.getStatus()).append("\",\n")
                .append("      \"duration\": ").append(result.getDuration().toMillis()).append(",\n")
                .append("      \"message\": \"").append(result.getMessage() != null ? result.getMessage() : "").append("\"\n")
                .append("    }").append(it.hasNext() ? "," : "").append("\n");
        }

        json.append("  ]\n}");
        return json.toString();
    }

    private static String generateSummaryContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n<head>\n")
            .append("<title>Test Execution Summary</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append(".suite { margin-bottom: 20px; padding: 10px; border: 1px solid #ddd; }\n")
            .append(".passed { color: green; }\n")
            .append(".failed { color: red; }\n")
            .append(".skipped { color: orange; }\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>Test Execution Summary</h1>\n")
            .append("<p>Total Test Suites: ").append(suites.size()).append("</p>\n")
            .append("<p>Total Tests: ").append(totalTests.get()).append("</p>\n");

        for (TestSuite suite : suites.values()) {
            html.append("<div class=\"suite\">\n")
                .append("<h2>").append(suite.getName()).append("</h2>\n")
                .append("<p>Total: ").append(suite.getTotalTests()).append("</p>\n")
                .append("<p class=\"passed\">Passed: ").append(suite.getPassedTests()).append("</p>\n")
                .append("<p class=\"failed\">Failed: ").append(suite.getFailedTests()).append("</p>\n")
                .append("<p class=\"skipped\">Skipped: ").append(suite.getSkippedTests()).append("</p>\n")
                .append("<p>Duration: ").append(suite.getDuration().toMillis()).append("ms</p>\n")
                .append("</div>\n");
        }

        html.append("</body>\n</html>");
        return html.toString();
    }

    private static void writeReport(String name, String content, String extension) throws IOException {
        Path reportsDir = Paths.get(REPORTS_DIR);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("%s_%s.%s", name, timestamp, extension);
        Path reportPath = reportsDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(reportPath.toFile())) {
            writer.write(content);
        }
    }

    private static class TestSuite {
        private final String name;
        private final LocalDateTime startTime;
        private final List<TestResult> results;

        TestSuite(String name) {
            this.name = name;
            this.startTime = LocalDateTime.now();
            this.results = new ArrayList<>();
        }

        void addResult(TestResult result) {
            results.add(result);
        }

        String getName() { return name; }
        List<TestResult> getResults() { return results; }
        int getTotalTests() { return results.size(); }
        
        int getPassedTests() {
            return (int) results.stream()
                .filter(r -> "PASSED".equals(r.getStatus()))
                .count();
        }
        
        int getFailedTests() {
            return (int) results.stream()
                .filter(r -> "FAILED".equals(r.getStatus()))
                .count();
        }
        
        int getSkippedTests() {
            return (int) results.stream()
                .filter(r -> "SKIPPED".equals(r.getStatus()))
                .count();
        }
        
        Duration getDuration() {
            return Duration.between(startTime, LocalDateTime.now());
        }
    }

    public static class TestResult {
        private final String testName;
        private final String status;
        private final Duration duration;
        private final String message;

        public TestResult(String testName, String status, Duration duration, String message) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.message = message;
        }

        String getTestName() { return testName; }
        String getStatus() { return status; }
        Duration getDuration() { return duration; }
        String getMessage() { return message; }
    }
}
