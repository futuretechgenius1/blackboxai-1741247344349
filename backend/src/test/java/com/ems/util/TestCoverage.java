package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing and reporting test coverage metrics
 */
public final class TestCoverage {

    private static final Logger logger = LoggerFactory.getLogger(TestCoverage.class);
    private static final String COVERAGE_DIR = "test-coverage";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Map<String, CoverageData> coverageMap = new ConcurrentHashMap<>();
    private static final double COVERAGE_THRESHOLD = 0.80; // 80% coverage threshold

    private TestCoverage() {
        // Private constructor to prevent instantiation
    }

    /**
     * Record coverage for a class
     */
    public static void recordClassCoverage(String className, CoverageData coverage) {
        coverageMap.put(className, coverage);
        logger.debug("Recorded coverage for class: {}", className);
    }

    /**
     * Generate coverage report
     */
    public static void generateReport() {
        try {
            String htmlContent = generateHtmlReport();
            String jsonContent = generateJsonReport();
            
            writeCoverageReport("coverage", htmlContent, "html");
            writeCoverageReport("coverage", jsonContent, "json");
            
            logger.info("Generated coverage reports");
        } catch (IOException e) {
            logger.error("Failed to generate coverage reports", e);
        }
    }

    /**
     * Get overall coverage metrics
     */
    public static CoverageSummary getOverallCoverage() {
        int totalLines = 0;
        int coveredLines = 0;
        int totalBranches = 0;
        int coveredBranches = 0;
        int totalMethods = 0;
        int coveredMethods = 0;

        for (CoverageData data : coverageMap.values()) {
            totalLines += data.getTotalLines();
            coveredLines += data.getCoveredLines();
            totalBranches += data.getTotalBranches();
            coveredBranches += data.getCoveredBranches();
            totalMethods += data.getTotalMethods();
            coveredMethods += data.getCoveredMethods();
        }

        return new CoverageSummary(
            calculatePercentage(coveredLines, totalLines),
            calculatePercentage(coveredBranches, totalBranches),
            calculatePercentage(coveredMethods, totalMethods)
        );
    }

    /**
     * Check if coverage meets threshold
     */
    public static boolean meetsThreshold() {
        CoverageSummary summary = getOverallCoverage();
        return summary.getLineCoverage() >= COVERAGE_THRESHOLD &&
               summary.getBranchCoverage() >= COVERAGE_THRESHOLD &&
               summary.getMethodCoverage() >= COVERAGE_THRESHOLD;
    }

    private static String generateHtmlReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n<head>\n")
            .append("<title>Test Coverage Report</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("table { border-collapse: collapse; width: 100%; }\n")
            .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("th { background-color: #f2f2f2; }\n")
            .append(".good { color: green; }\n")
            .append(".warning { color: orange; }\n")
            .append(".poor { color: red; }\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>Test Coverage Report</h1>\n")
            .append("<p>Generated: ").append(LocalDateTime.now()).append("</p>\n");

        // Overall summary
        CoverageSummary summary = getOverallCoverage();
        html.append("<h2>Overall Coverage</h2>\n")
            .append("<ul>\n")
            .append("<li>Line Coverage: ").append(formatPercentage(summary.getLineCoverage())).append("</li>\n")
            .append("<li>Branch Coverage: ").append(formatPercentage(summary.getBranchCoverage())).append("</li>\n")
            .append("<li>Method Coverage: ").append(formatPercentage(summary.getMethodCoverage())).append("</li>\n")
            .append("</ul>\n");

        // Class details
        html.append("<h2>Class Coverage Details</h2>\n")
            .append("<table>\n")
            .append("<tr><th>Class</th><th>Line Coverage</th><th>Branch Coverage</th><th>Method Coverage</th></tr>\n");

        coverageMap.forEach((className, coverage) -> {
            html.append("<tr>\n")
                .append("<td>").append(className).append("</td>\n")
                .append("<td>").append(formatPercentage(coverage.getLineCoveragePercentage())).append("</td>\n")
                .append("<td>").append(formatPercentage(coverage.getBranchCoveragePercentage())).append("</td>\n")
                .append("<td>").append(formatPercentage(coverage.getMethodCoveragePercentage())).append("</td>\n")
                .append("</tr>\n");
        });

        html.append("</table>\n</body>\n</html>");
        return html.toString();
    }

    private static String generateJsonReport() {
        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"timestamp\": \"").append(LocalDateTime.now()).append("\",\n")
            .append("  \"summary\": ").append(generateJsonSummary()).append(",\n")
            .append("  \"classes\": ").append(generateJsonClasses()).append("\n")
            .append("}");
        return json.toString();
    }

    private static String generateJsonSummary() {
        CoverageSummary summary = getOverallCoverage();
        return String.format(
            "{\n    \"lineCoverage\": %.2f,\n    \"branchCoverage\": %.2f,\n    \"methodCoverage\": %.2f\n  }",
            summary.getLineCoverage(),
            summary.getBranchCoverage(),
            summary.getMethodCoverage()
        );
    }

    private static String generateJsonClasses() {
        return coverageMap.entrySet().stream()
            .map(entry -> String.format(
                "    \"%s\": {\n      \"lineCoverage\": %.2f,\n      \"branchCoverage\": %.2f,\n      \"methodCoverage\": %.2f\n    }",
                entry.getKey(),
                entry.getValue().getLineCoveragePercentage(),
                entry.getValue().getBranchCoveragePercentage(),
                entry.getValue().getMethodCoveragePercentage()
            ))
            .collect(Collectors.joining(",\n", "{\n", "\n  }"));
    }

    private static void writeCoverageReport(String name, String content, String extension) throws IOException {
        Path coverageDir = Paths.get(COVERAGE_DIR);
        if (!Files.exists(coverageDir)) {
            Files.createDirectories(coverageDir);
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("%s_%s.%s", name, timestamp, extension);
        Path reportPath = coverageDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(reportPath.toFile())) {
            writer.write(content);
        }
    }

    private static double calculatePercentage(int covered, int total) {
        return total == 0 ? 0.0 : (covered * 100.0) / total;
    }

    private static String formatPercentage(double percentage) {
        return String.format("%.2f%%", percentage);
    }

    /**
     * Coverage data class
     */
    public static class CoverageData {
        private final int totalLines;
        private final int coveredLines;
        private final int totalBranches;
        private final int coveredBranches;
        private final int totalMethods;
        private final int coveredMethods;

        public CoverageData(int totalLines, int coveredLines, int totalBranches, 
                          int coveredBranches, int totalMethods, int coveredMethods) {
            this.totalLines = totalLines;
            this.coveredLines = coveredLines;
            this.totalBranches = totalBranches;
            this.coveredBranches = coveredBranches;
            this.totalMethods = totalMethods;
            this.coveredMethods = coveredMethods;
        }

        public int getTotalLines() { return totalLines; }
        public int getCoveredLines() { return coveredLines; }
        public int getTotalBranches() { return totalBranches; }
        public int getCoveredBranches() { return coveredBranches; }
        public int getTotalMethods() { return totalMethods; }
        public int getCoveredMethods() { return coveredMethods; }

        public double getLineCoveragePercentage() {
            return calculatePercentage(coveredLines, totalLines);
        }

        public double getBranchCoveragePercentage() {
            return calculatePercentage(coveredBranches, totalBranches);
        }

        public double getMethodCoveragePercentage() {
            return calculatePercentage(coveredMethods, totalMethods);
        }
    }

    /**
     * Coverage summary class
     */
    public static class CoverageSummary {
        private final double lineCoverage;
        private final double branchCoverage;
        private final double methodCoverage;

        public CoverageSummary(double lineCoverage, double branchCoverage, double methodCoverage) {
            this.lineCoverage = lineCoverage;
            this.branchCoverage = branchCoverage;
            this.methodCoverage = methodCoverage;
        }

        public double getLineCoverage() { return lineCoverage; }
        public double getBranchCoverage() { return branchCoverage; }
        public double getMethodCoverage() { return methodCoverage; }
    }
}
