package com.ems.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomExceptionsTest {

    @Test
    void resourceNotFoundExceptionWithMessage() {
        // Arrange
        String message = "Resource not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void resourceNotFoundExceptionWithMessageAndCause() {
        // Arrange
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Original cause");

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void duplicateResourceExceptionWithMessage() {
        // Arrange
        String message = "Duplicate resource";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void duplicateResourceExceptionWithMessageAndCause() {
        // Arrange
        String message = "Duplicate resource";
        Throwable cause = new RuntimeException("Original cause");

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void unauthorizedAccessExceptionWithMessage() {
        // Arrange
        String message = "Unauthorized access";

        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void unauthorizedAccessExceptionWithMessageAndCause() {
        // Arrange
        String message = "Unauthorized access";
        Throwable cause = new RuntimeException("Original cause");

        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void payrollProcessingExceptionWithMessage() {
        // Arrange
        String message = "Payroll processing error";

        // Act
        PayrollProcessingException exception = new PayrollProcessingException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void payrollProcessingExceptionWithMessageAndCause() {
        // Arrange
        String message = "Payroll processing error";
        Throwable cause = new RuntimeException("Original cause");

        // Act
        PayrollProcessingException exception = new PayrollProcessingException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exceptionInheritance() {
        // Assert
        assertTrue(new ResourceNotFoundException("") instanceof RuntimeException);
        assertTrue(new DuplicateResourceException("") instanceof RuntimeException);
        assertTrue(new UnauthorizedAccessException("") instanceof RuntimeException);
        assertTrue(new PayrollProcessingException("") instanceof RuntimeException);
    }

    @Test
    void exceptionStackTrace() {
        // Arrange
        RuntimeException originalCause = new RuntimeException("Original cause");
        ResourceNotFoundException exception = new ResourceNotFoundException("Test", originalCause);

        // Act
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Assert
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void exceptionSuppression() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        RuntimeException suppressed = new RuntimeException("Suppressed");

        // Act
        exception.addSuppressed(suppressed);

        // Assert
        assertEquals(1, exception.getSuppressed().length);
        assertEquals(suppressed, exception.getSuppressed()[0]);
    }

    @Test
    void customExceptionsEquality() {
        // Arrange
        String message = "Test message";
        ResourceNotFoundException exception1 = new ResourceNotFoundException(message);
        ResourceNotFoundException exception2 = new ResourceNotFoundException(message);

        // Assert
        assertNotEquals(exception1, exception2); // Exceptions should not be equal even with same message
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }
}
