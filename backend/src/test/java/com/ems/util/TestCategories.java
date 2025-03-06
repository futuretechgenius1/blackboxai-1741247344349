package com.ems.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test categories and related utilities for the EMS application
 */
public final class TestCategories {

    private TestCategories() {
        // Private constructor to prevent instantiation
    }

    /**
     * Test type categories
     */
    public static final class Type {
        public static final String UNIT = "unit";
        public static final String INTEGRATION = "integration";
        public static final String FUNCTIONAL = "functional";
        public static final String PERFORMANCE = "performance";
        public static final String SECURITY = "security";
        public static final String SMOKE = "smoke";
        public static final String REGRESSION = "regression";
        public static final String ACCEPTANCE = "acceptance";
        public static final String LOAD = "load";
        public static final String STRESS = "stress";
    }

    /**
     * Component categories
     */
    public static final class Component {
        public static final String CONTROLLER = "controller";
        public static final String SERVICE = "service";
        public static final String REPOSITORY = "repository";
        public static final String SECURITY = "security";
        public static final String CONFIG = "config";
        public static final String MODEL = "model";
        public static final String DTO = "dto";
        public static final String UTIL = "util";
    }

    /**
     * Feature categories
     */
    public static final class Feature {
        public static final String AUTH = "authentication";
        public static final String USER = "user-management";
        public static final String WORKLOG = "work-log";
        public static final String PAYROLL = "payroll";
        public static final String REPORTING = "reporting";
        public static final String ADMIN = "admin";
        public static final String API = "api";
    }

    /**
     * Priority categories
     */
    public static final class Priority {
        public static final String P0 = "p0-critical";
        public static final String P1 = "p1-high";
        public static final String P2 = "p2-medium";
        public static final String P3 = "p3-low";
    }

    /**
     * Environment categories
     */
    public static final class Environment {
        public static final String DEV = "development";
        public static final String TEST = "test";
        public static final String STAGE = "staging";
        public static final String PROD = "production";
    }

    /**
     * Status categories
     */
    public static final class Status {
        public static final String STABLE = "stable";
        public static final String UNSTABLE = "unstable";
        public static final String FLAKY = "flaky";
        public static final String EXPERIMENTAL = "experimental";
    }

    /**
     * Duration categories
     */
    public static final class Duration {
        public static final String FAST = "fast";
        public static final String MEDIUM = "medium";
        public static final String SLOW = "slow";
    }

    // Category annotations
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestType {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestComponent {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestFeature {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestPriority {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestEnvironment {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestStatus {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestDuration {
        String value();
    }

    // Combined category annotation
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestCategories {
        TestType type();
        TestComponent component();
        TestFeature feature();
        TestPriority priority();
        TestEnvironment environment() default @TestEnvironment(Environment.TEST);
        TestStatus status() default @TestStatus(Status.STABLE);
        TestDuration duration() default @TestDuration(Duration.FAST);
    }

    // Utility methods
    public static boolean isUnitTest(String category) {
        return Type.UNIT.equals(category);
    }

    public static boolean isIntegrationTest(String category) {
        return Type.INTEGRATION.equals(category);
    }

    public static boolean isPerformanceTest(String category) {
        return Type.PERFORMANCE.equals(category);
    }

    public static boolean isSecurityTest(String category) {
        return Type.SECURITY.equals(category);
    }

    public static boolean isCriticalPriority(String priority) {
        return Priority.P0.equals(priority);
    }

    public static boolean isStableTest(String status) {
        return Status.STABLE.equals(status);
    }

    public static boolean isFastTest(String duration) {
        return Duration.FAST.equals(duration);
    }

    public static boolean isProductionTest(String environment) {
        return Environment.PROD.equals(environment);
    }
}
