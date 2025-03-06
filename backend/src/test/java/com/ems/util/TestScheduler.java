package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Utility class for managing test scheduling and timing
 */
public final class TestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TestScheduler.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private TestScheduler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Schedule a task to run after a delay
     */
    public static ScheduledFuture<?> scheduleTask(String taskId, Runnable task, Duration delay) {
        logger.debug("Scheduling task {} to run after {} ms", taskId, delay.toMillis());
        ScheduledFuture<?> future = scheduler.schedule(
            wrapTask(taskId, task),
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        );
        scheduledTasks.put(taskId, future);
        return future;
    }

    /**
     * Schedule a task to run periodically
     */
    public static ScheduledFuture<?> schedulePeriodicTask(String taskId, Runnable task, 
            Duration initialDelay, Duration period) {
        logger.debug("Scheduling periodic task {} with period {} ms", taskId, period.toMillis());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            wrapTask(taskId, task),
            initialDelay.toMillis(),
            period.toMillis(),
            TimeUnit.MILLISECONDS
        );
        scheduledTasks.put(taskId, future);
        return future;
    }

    /**
     * Cancel a scheduled task
     */
    public static void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            logger.debug("Cancelled task {}", taskId);
        }
    }

    /**
     * Execute task with timeout
     */
    public static <T> T executeWithTimeout(String taskId, Supplier<T> task, Duration timeout) 
            throws TimeoutException, ExecutionException, InterruptedException {
        logger.debug("Executing task {} with timeout {} ms", taskId, timeout.toMillis());
        Future<T> future = scheduler.submit(() -> task.get());
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Execute task with retry
     */
    public static <T> T executeWithRetry(String taskId, Supplier<T> task, int maxRetries, 
            Duration delayBetweenRetries) {
        logger.debug("Executing task {} with {} retries", taskId, maxRetries);
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return task.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                logger.warn("Attempt {} failed for task {}: {}", 
                    attempts, taskId, e.getMessage());

                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(delayBetweenRetries.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Task failed after " + maxRetries + " attempts", 
            lastException);
    }

    /**
     * Wait for condition with timeout
     */
    public static boolean waitForCondition(String conditionId, Supplier<Boolean> condition, 
            Duration timeout, Duration checkInterval) {
        logger.debug("Waiting for condition {} with timeout {} ms", conditionId, timeout.toMillis());
        Instant startTime = Instant.now();
        
        while (Duration.between(startTime, Instant.now()).compareTo(timeout) < 0) {
            if (condition.get()) {
                return true;
            }
            try {
                Thread.sleep(checkInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }

    /**
     * Shutdown scheduler
     */
    public static void shutdown() {
        logger.info("Shutting down test scheduler");
        scheduledTasks.forEach((taskId, future) -> future.cancel(true));
        scheduledTasks.clear();
        scheduler.shutdownNow();
    }

    /**
     * Check if scheduler is active
     */
    public static boolean isActive() {
        return !scheduler.isShutdown();
    }

    /**
     * Get active task count
     */
    public static int getActiveTaskCount() {
        return scheduledTasks.size();
    }

    /**
     * Wrap task with logging and error handling
     */
    private static Runnable wrapTask(String taskId, Runnable task) {
        return () -> {
            Instant startTime = Instant.now();
            try {
                logger.debug("Starting task {}", taskId);
                task.run();
                Duration duration = Duration.between(startTime, Instant.now());
                logger.debug("Completed task {} in {} ms", taskId, duration.toMillis());
            } catch (Exception e) {
                logger.error("Task {} failed: {}", taskId, e.getMessage(), e);
                throw e;
            } finally {
                scheduledTasks.remove(taskId);
            }
        };
    }

    /**
     * Task execution result
     */
    public static class TaskResult<T> {
        private final T result;
        private final Duration duration;
        private final boolean successful;
        private final Exception error;

        private TaskResult(T result, Duration duration, boolean successful, Exception error) {
            this.result = result;
            this.duration = duration;
            this.successful = successful;
            this.error = error;
        }

        public static <T> TaskResult<T> success(T result, Duration duration) {
            return new TaskResult<>(result, duration, true, null);
        }

        public static <T> TaskResult<T> failure(Exception error, Duration duration) {
            return new TaskResult<>(null, duration, false, error);
        }

        public T getResult() { return result; }
        public Duration getDuration() { return duration; }
        public boolean isSuccessful() { return successful; }
        public Exception getError() { return error; }
    }
}
