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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for measuring and analyzing test performance
 */
public final class TestPerformance {

    private static final Logger logger = LoggerFactory.getLogger(TestPerformance.class);
    private static final Map<String, PerformanceMetrics> metricsMap = new ConcurrentHashMap<>();
    private static final Map<String, Instant> startTimes = new ConcurrentHashMap<>();
    private static final long SLOW_TEST_THRESHOLD = 1000; // 1 second

    private TestPerformance() {
        // Private constructor to prevent instantiation
    }

    /**
     * Start measuring performance for a test
     */
    public static void startMeasurement(String testName) {
        startTimes.put(testName, Instant.now());
        metricsMap.putIfAbsent(testName, new PerformanceMetrics(testName));
    }

    /**
     * Stop measuring performance for a test
     */
    public static void stopMeasurement(String testName) {
        Instant startTime = startTimes.remove(testName);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            PerformanceMetrics metrics = metricsMap.get(testName);
            if (metrics != null) {
                metrics.addExecution(duration);
                
                if (duration.toMillis() > SLOW_TEST_THRESHOLD) {
                    logger.warn("Slow test detected: {} took {} ms", 
                        testName, duration.toMillis());
                }
            }
        }
    }

    /**
     * Record a specific metric
     */
    public static void recordMetric(String testName, String metricName, long value) {
        PerformanceMetrics metrics = metricsMap.get(testName);
        if (metrics != null) {
            metrics.addCustomMetric(metricName, value);
        }
    }

    /**
     * Get performance report for a test
     */
    public static PerformanceReport getReport(String testName) {
        PerformanceMetrics metrics = metricsMap.get(testName);
        return metrics != null ? metrics.generateReport() : null;
    }

    /**
     * Get performance summary for all tests
     */
    public static List<PerformanceReport> getAllReports() {
        List<PerformanceReport> reports = new ArrayList<>();
        metricsMap.values().forEach(metrics -> 
            reports.add(metrics.generateReport()));
        return reports;
    }

    /**
     * Clear all performance metrics
     */
    public static void clearMetrics() {
        metricsMap.clear();
        startTimes.clear();
    }

    /**
     * Performance metrics tracking class
     */
    private static class PerformanceMetrics {
        private final String testName;
        private final List<Duration> executionTimes;
        private final Map<String, List<Long>> customMetrics;
        private Duration minDuration;
        private Duration maxDuration;
        private final AtomicLong totalExecutions;

        PerformanceMetrics(String testName) {
            this.testName = testName;
            this.executionTimes = new ArrayList<>();
            this.customMetrics = new HashMap<>();
            this.totalExecutions = new AtomicLong(0);
        }

        synchronized void addExecution(Duration duration) {
            executionTimes.add(duration);
            totalExecutions.incrementAndGet();

            if (minDuration == null || duration.compareTo(minDuration) < 0) {
                minDuration = duration;
            }
            if (maxDuration == null || duration.compareTo(maxDuration) > 0) {
                maxDuration = duration;
            }
        }

        void addCustomMetric(String metricName, long value) {
            customMetrics.computeIfAbsent(metricName, k -> new ArrayList<>())
                .add(value);
        }

        PerformanceReport generateReport() {
            return new PerformanceReport(
                testName,
                calculateAverageDuration(),
                minDuration,
                maxDuration,
                totalExecutions.get(),
                calculateStandardDeviation(),
                new HashMap<>(calculateCustomMetricStats())
            );
        }

        private Duration calculateAverageDuration() {
            if (executionTimes.isEmpty()) return Duration.ZERO;
            
            long totalMillis = executionTimes.stream()
                .mapToLong(Duration::toMillis)
                .sum();
            return Duration.ofMillis(totalMillis / executionTimes.size());
        }

        private double calculateStandardDeviation() {
            if (executionTimes.isEmpty()) return 0.0;

            double mean = executionTimes.stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0.0);

            double sumSquaredDiff = executionTimes.stream()
                .mapToDouble(d -> {
                    double diff = d.toMillis() - mean;
                    return diff * diff;
                })
                .sum();

            return Math.sqrt(sumSquaredDiff / executionTimes.size());
        }

        private Map<String, MetricStats> calculateCustomMetricStats() {
            Map<String, MetricStats> stats = new HashMap<>();
            
            customMetrics.forEach((name, values) -> {
                if (!values.isEmpty()) {
                    double avg = values.stream()
                        .mapToLong(v -> v)
                        .average()
                        .orElse(0.0);
                    
                    long min = values.stream()
                        .mapToLong(v -> v)
                        .min()
                        .orElse(0);
                    
                    long max = values.stream()
                        .mapToLong(v -> v)
                        .max()
                        .orElse(0);

                    stats.put(name, new MetricStats(avg, min, max));
                }
            });

            return stats;
        }
    }

    /**
     * Performance report class
     */
    public static class PerformanceReport {
        private final String testName;
        private final Duration averageDuration;
        private final Duration minDuration;
        private final Duration maxDuration;
        private final long totalExecutions;
        private final double standardDeviation;
        private final Map<String, MetricStats> customMetrics;

        PerformanceReport(String testName, Duration averageDuration, 
                Duration minDuration, Duration maxDuration, long totalExecutions,
                double standardDeviation, Map<String, MetricStats> customMetrics) {
            this.testName = testName;
            this.averageDuration = averageDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.totalExecutions = totalExecutions;
            this.standardDeviation = standardDeviation;
            this.customMetrics = customMetrics;
        }

        public String getTestName() { return testName; }
        public Duration getAverageDuration() { return averageDuration; }
        public Duration getMinDuration() { return minDuration; }
        public Duration getMaxDuration() { return maxDuration; }
        public long getTotalExecutions() { return totalExecutions; }
        public double getStandardDeviation() { return standardDeviation; }
        public Map<String, MetricStats> getCustomMetrics() { return customMetrics; }
    }

    /**
     * Metric statistics class
     */
    public static class MetricStats {
        private final double average;
        private final long min;
        private final long max;

        MetricStats(double average, long min, long max) {
            this.average = average;
            this.min = min;
            this.max = max;
        }

        public double getAverage() { return average; }
        public long getMin() { return min; }
        public long getMax() { return max; }
    }
}
