package com.ems.util;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Utility class for managing test mocks and stubs
 */
public final class TestMock {

    private static final Logger logger = LoggerFactory.getLogger(TestMock.class);
    private static final ConcurrentHashMap<String, Object> mockRegistry = new ConcurrentHashMap<>();

    private TestMock() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create and register a mock
     */
    public static <T> T createMock(String mockId, Class<T> classToMock) {
        T mock = mock(classToMock);
        mockRegistry.put(mockId, mock);
        logger.debug("Created mock {} for class {}", mockId, classToMock.getSimpleName());
        return mock;
    }

    /**
     * Get registered mock
     */
    @SuppressWarnings("unchecked")
    public static <T> T getMock(String mockId) {
        return (T) mockRegistry.get(mockId);
    }

    /**
     * Remove mock from registry
     */
    public static void removeMock(String mockId) {
        mockRegistry.remove(mockId);
        logger.debug("Removed mock {}", mockId);
    }

    /**
     * Clear all mocks
     */
    public static void clearMocks() {
        mockRegistry.clear();
        logger.debug("Cleared all mocks");
    }

    /**
     * Create mock user
     */
    public static User createMockUser(Role role) {
        return User.builder()
            .id(1L)
            .username("test.user")
            .email("test.user@ems.com")
            .password("Test@123")
            .firstName("Test")
            .lastName("User")
            .role(role)
            .department("IT")
            .position("Software Engineer")
            .hourlyRate(25.0)
            .enabled(true)
            .build();
    }

    /**
     * Create mock work log
     */
    public static WorkLog createMockWorkLog(User user) {
        return WorkLog.builder()
            .id(1L)
            .user(user)
            .date(LocalDate.now())
            .hoursWorked(8.0)
            .remarks("Test work log")
            .status(WorkLog.WorkLogStatus.PENDING)
            .build();
    }

    /**
     * Create mock authentication
     */
    public static Authentication createMockAuthentication(User user) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(auth.getAuthorities())
            .thenReturn(List.of(new SimpleGrantedAuthority(user.getRole().name())));
        return auth;
    }

    /**
     * Create incremental ID generator
     */
    public static Answer<Long> createIncrementalIdGenerator() {
        return new Answer<>() {
            private long counter = 1;
            @Override
            public Long answer(InvocationOnMock invocation) {
                return counter++;
            }
        };
    }

    /**
     * Create list collector answer
     */
    public static <T> Answer<T> createListCollector(List<T> list) {
        return invocation -> {
            T arg = invocation.getArgument(0);
            list.add(arg);
            return arg;
        };
    }

    /**
     * Create delayed answer
     */
    public static <T> Answer<T> createDelayedAnswer(T result, long delayMillis) {
        return invocation -> {
            Thread.sleep(delayMillis);
            return result;
        };
    }

    /**
     * Create exception throwing answer
     */
    public static Answer<Object> createExceptionAnswer(Exception exception) {
        return invocation -> {
            throw exception;
        };
    }

    /**
     * Create mock verifier
     */
    public static class MockVerifier<T> {
        private final T mock;
        private final List<VerificationError> errors;

        public MockVerifier(T mock) {
            this.mock = mock;
            this.errors = new ArrayList<>();
        }

        public MockVerifier<T> verify(Function<T, Object> interaction) {
            try {
                interaction.apply(mock);
            } catch (AssertionError e) {
                errors.add(new VerificationError(e.getMessage()));
            }
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<VerificationError> getErrors() {
            return errors;
        }
    }

    /**
     * Create argument matcher
     */
    public static <T> ArgumentMatcher<T> matchesPredicate(Function<T, Boolean> predicate) {
        return new ArgumentMatcher<>() {
            @Override
            public boolean matches(T argument) {
                return predicate.apply(argument);
            }
        };
    }

    /**
     * Mock registry entry
     */
    private static class MockRegistryEntry {
        private final Object mock;
        private final LocalDateTime createdAt;
        private int invocationCount;

        MockRegistryEntry(Object mock) {
            this.mock = mock;
            this.createdAt = LocalDateTime.now();
            this.invocationCount = 0;
        }

        void incrementInvocationCount() {
            invocationCount++;
        }

        Object getMock() { return mock; }
        LocalDateTime getCreatedAt() { return createdAt; }
        int getInvocationCount() { return invocationCount; }
    }

    /**
     * Verification error
     */
    public static class VerificationError {
        private final String message;
        private final LocalDateTime timestamp;

        VerificationError(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
