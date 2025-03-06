package com.ems.util;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing test context and state
 */
public final class TestContext {

    private static final ThreadLocal<Map<String, Object>> contextHolder = ThreadLocal.withInitial(HashMap::new);
    private static final Map<String, Object> globalContext = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> testStartTimes = new ConcurrentHashMap<>();

    private TestContext() {
        // Private constructor to prevent instantiation
    }

    /**
     * Context management methods
     */
    public static void setContextValue(String key, Object value) {
        contextHolder.get().put(key, value);
    }

    public static Object getContextValue(String key) {
        return contextHolder.get().get(key);
    }

    public static void removeContextValue(String key) {
        contextHolder.get().remove(key);
    }

    public static void clearContext() {
        contextHolder.get().clear();
    }

    /**
     * Global context methods
     */
    public static void setGlobalValue(String key, Object value) {
        globalContext.put(key, value);
    }

    public static Object getGlobalValue(String key) {
        return globalContext.get(key);
    }

    public static void removeGlobalValue(String key) {
        globalContext.remove(key);
    }

    public static void clearGlobalContext() {
        globalContext.clear();
    }

    /**
     * Security context methods
     */
    public static void setAuthentication(Authentication auth) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * MockMvc context methods
     */
    public static MockMvc createMockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    /**
     * Test timing methods
     */
    public static void startTest(String testName) {
        testStartTimes.put(testName, LocalDateTime.now());
    }

    public static LocalDateTime getTestStartTime(String testName) {
        return testStartTimes.get(testName);
    }

    public static void endTest(String testName) {
        testStartTimes.remove(testName);
    }

    /**
     * Test user context methods
     */
    public static void setCurrentUser(User user) {
        setContextValue("currentUser", user);
    }

    public static User getCurrentUser() {
        return (User) getContextValue("currentUser");
    }

    public static void setTestAdmin() {
        User admin = TestDataBuilder.buildTestAdmin();
        setCurrentUser(admin);
        TestUtils.setAuthentication(admin);
    }

    public static void setTestEmployee() {
        User employee = TestDataBuilder.buildTestEmployee();
        setCurrentUser(employee);
        TestUtils.setAuthentication(employee);
    }

    /**
     * Test data context methods
     */
    public static void setTestData(String key, Object value) {
        setContextValue("testData." + key, value);
    }

    public static Object getTestData(String key) {
        return getContextValue("testData." + key);
    }

    public static void clearTestData() {
        contextHolder.get().keySet().removeIf(key -> key.startsWith("testData."));
    }

    /**
     * Test state management methods
     */
    public static void markTestStarted(String testName) {
        setContextValue("test." + testName + ".started", true);
        startTest(testName);
    }

    public static void markTestCompleted(String testName) {
        setContextValue("test." + testName + ".completed", true);
        endTest(testName);
    }

    public static boolean isTestStarted(String testName) {
        return Boolean.TRUE.equals(getContextValue("test." + testName + ".started"));
    }

    public static boolean isTestCompleted(String testName) {
        return Boolean.TRUE.equals(getContextValue("test." + testName + ".completed"));
    }

    /**
     * Test environment methods
     */
    public static void setTestEnvironment(String environment) {
        setGlobalValue("testEnvironment", environment);
    }

    public static String getTestEnvironment() {
        return (String) getGlobalValue("testEnvironment");
    }

    /**
     * Test transaction methods
     */
    public static void beginTransaction() {
        setContextValue("transaction.active", true);
    }

    public static void commitTransaction() {
        setContextValue("transaction.active", false);
    }

    public static void rollbackTransaction() {
        setContextValue("transaction.active", false);
    }

    public static boolean isTransactionActive() {
        return Boolean.TRUE.equals(getContextValue("transaction.active"));
    }

    /**
     * Test cleanup methods
     */
    public static void registerCleanup(Runnable cleanup) {
        @SuppressWarnings("unchecked")
        List<Runnable> cleanups = (List<Runnable>) getContextValue("cleanups");
        if (cleanups == null) {
            cleanups = new ArrayList<>();
            setContextValue("cleanups", cleanups);
        }
        cleanups.add(cleanup);
    }

    public static void runCleanups() {
        @SuppressWarnings("unchecked")
        List<Runnable> cleanups = (List<Runnable>) getContextValue("cleanups");
        if (cleanups != null) {
            cleanups.forEach(Runnable::run);
            cleanups.clear();
        }
    }

    /**
     * Test metrics methods
     */
    public static void incrementTestCount() {
        Integer count = (Integer) getGlobalValue("testCount");
        setGlobalValue("testCount", count == null ? 1 : count + 1);
    }

    public static int getTestCount() {
        Integer count = (Integer) getGlobalValue("testCount");
        return count == null ? 0 : count;
    }

    /**
     * Utility inner classes
     */
    private static class TestState {
        private final String name;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean completed;
        private Throwable error;

        TestState(String name) {
            this.name = name;
            this.startTime = LocalDateTime.now();
        }
    }
}
