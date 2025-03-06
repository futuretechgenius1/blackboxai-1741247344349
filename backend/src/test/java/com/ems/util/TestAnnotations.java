package com.ems.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Custom test annotations for the EMS application
 */
public final class TestAnnotations {

    private TestAnnotations() {
        // Private constructor to prevent instantiation
    }

    /**
     * Annotation for integration tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("integration")
    @Transactional
    @Rollback
    public @interface IntegrationTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for unit tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("unit")
    public @interface UnitTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for performance tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("performance")
    public @interface PerformanceTest {
        long threshold() default 1000L;
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for security tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("security")
    public @interface SecurityTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for API tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("api")
    public @interface ApiTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for database tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("database")
    @Transactional
    @Rollback
    public @interface DatabaseTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for smoke tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("smoke")
    public @interface SmokeTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for regression tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("regression")
    public @interface RegressionTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for slow tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("slow")
    public @interface SlowTest {
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 10000L;
    }

    /**
     * Annotation for flaky tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("flaky")
    public @interface FlakyTest {
        int retries() default 3;
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for authentication required tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("auth")
    public @interface AuthenticationRequired {
        String[] roles() default {};
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for cleanup required tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("cleanup")
    public @interface CleanupRequired {
        String[] resources() default {};
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }

    /**
     * Annotation for data preparation required tests
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Test
    @Tag("data")
    public @interface DataPreparationRequired {
        String[] datasets() default {};
        @AliasFor(annotation = Test.class, attribute = "timeout")
        long timeout() default 0L;
    }
}
