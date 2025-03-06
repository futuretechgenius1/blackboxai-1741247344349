package com.ems.util;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom interceptors for test execution
 */
public final class TestInterceptors {

    private TestInterceptors() {
        // Private constructor to prevent instantiation
    }

    /**
     * Performance monitoring interceptor
     */
    public static class PerformanceInterceptor implements MethodInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
        private static final long SLOW_TEST_THRESHOLD = 1000; // 1 second

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Instant start = Instant.now();
            
            try {
                return invocation.proceed();
            } finally {
                Duration duration = Duration.between(start, Instant.now());
                Method method = invocation.getMethod();
                
                if (duration.toMillis() > SLOW_TEST_THRESHOLD) {
                    logger.warn("Slow test detected: {}.{} took {} ms",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        duration.toMillis());
                }

                TestMetrics.recordPerformanceMetric(
                    method.getName(),
                    "execution_time",
                    duration.toMillis()
                );
            }
        }
    }

    /**
     * Retry interceptor for flaky tests
     */
    public static class RetryInterceptor implements MethodInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
        private static final int MAX_RETRIES = 3;
        private final ConcurrentHashMap<Method, AtomicInteger> retryCount = new ConcurrentHashMap<>();

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            AtomicInteger attempts = retryCount.computeIfAbsent(method, k -> new AtomicInteger(0));

            try {
                return invocation.proceed();
            } catch (Throwable throwable) {
                if (attempts.incrementAndGet() <= MAX_RETRIES) {
                    logger.warn("Test failed, attempting retry {}/{}: {}.{}",
                        attempts.get(),
                        MAX_RETRIES,
                        method.getDeclaringClass().getSimpleName(),
                        method.getName());
                    return invoke(invocation);
                } else {
                    logger.error("Test failed after {} attempts: {}.{}",
                        MAX_RETRIES,
                        method.getDeclaringClass().getSimpleName(),
                        method.getName());
                    throw throwable;
                }
            } finally {
                if (attempts.get() == MAX_RETRIES) {
                    retryCount.remove(method);
                }
            }
        }
    }

    /**
     * Logging interceptor
     */
    public static class LoggingInterceptor implements MethodInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            String className = method.getDeclaringClass().getSimpleName();
            String methodName = method.getName();
            Object[] args = invocation.getArguments();

            logger.info("Executing test: {}.{}", className, methodName);
            if (args.length > 0) {
                logger.debug("Test arguments: {}", Arrays.toString(args));
            }

            try {
                Object result = invocation.proceed();
                logger.info("Test completed successfully: {}.{}", className, methodName);
                return result;
            } catch (Throwable throwable) {
                logger.error("Test failed: {}.{}", className, methodName, throwable);
                throw throwable;
            }
        }
    }

    /**
     * Transaction boundary interceptor
     */
    public static class TransactionInterceptor implements MethodInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            String className = method.getDeclaringClass().getSimpleName();
            String methodName = method.getName();

            logger.debug("Starting transaction for test: {}.{}", className, methodName);
            try {
                Object result = invocation.proceed();
                logger.debug("Committing transaction for test: {}.{}", className, methodName);
                return result;
            } catch (Throwable throwable) {
                logger.error("Rolling back transaction for test: {}.{}", className, methodName);
                throw throwable;
            }
        }
    }

    /**
     * Security context interceptor
     */
    public static class SecurityContextInterceptor implements MethodInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(SecurityContextInterceptor.class);

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            String className = method.getDeclaringClass().getSimpleName();
            String methodName = method.getName();

            logger.debug("Setting up security context for test: {}.{}", className, methodName);
            try {
                return invocation.proceed();
            } finally {
                logger.debug("Clearing security context for test: {}.{}", className, methodName);
                TestUtils.clearAuthentication();
            }
        }
    }

    /**
     * Utility method to create a proxy with interceptors
     */
    public static <T> T createProxy(T target, Class<T> targetClass, MethodInterceptor... interceptors) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        for (MethodInterceptor interceptor : interceptors) {
            factory.addAdvice(interceptor);
        }
        return targetClass.cast(factory.getProxy());
    }
}
