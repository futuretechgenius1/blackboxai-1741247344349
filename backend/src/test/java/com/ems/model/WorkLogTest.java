package com.ems.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WorkLogTest {

    private Validator validator;
    private WorkLog workLog;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = User.builder()
                .id(1L)
                .username("test.user")
                .email("test@example.com")
                .hourlyRate(25.0)
                .role(Role.ROLE_EMPLOYEE)
                .build();

        workLog = WorkLog.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Regular work day")
                .status(WorkLog.WorkLogStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void validWorkLog() {
        // Act
        Set<ConstraintViolation<WorkLog>> violations = validator.validate(workLog);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidWorkLogWithNullUser() {
        // Arrange
        workLog.setUser(null);

        // Act
        Set<ConstraintViolation<WorkLog>> violations = validator.validate(workLog);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("user")));
    }

    @Test
    void invalidWorkLogWithNullDate() {
        // Arrange
        workLog.setDate(null);

        // Act
        Set<ConstraintViolation<WorkLog>> violations = validator.validate(workLog);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("date")));
    }

    @Test
    void invalidWorkLogWithNegativeHours() {
        // Arrange
        workLog.setHoursWorked(-1.0);

        // Act
        Set<ConstraintViolation<WorkLog>> violations = validator.validate(workLog);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hoursWorked")));
    }

    @Test
    void invalidWorkLogWithExcessiveHours() {
        // Arrange
        workLog.setHoursWorked(25.0);

        // Act
        Set<ConstraintViolation<WorkLog>> violations = validator.validate(workLog);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hoursWorked")));
    }

    @Test
    void calculateRegularHours() {
        // Arrange
        workLog.setHoursWorked(10.0);

        // Act
        double regularHours = workLog.getRegularHours();

        // Assert
        assertEquals(8.0, regularHours); // Regular hours capped at 8
    }

    @Test
    void calculateOvertimeHours() {
        // Arrange
        workLog.setHoursWorked(10.0);

        // Act
        double overtimeHours = workLog.getOvertimeHours();

        // Assert
        assertEquals(2.0, overtimeHours); // 10 - 8 = 2 overtime hours
    }

    @Test
    void calculateRegularPay() {
        // Arrange
        workLog.setHoursWorked(8.0);

        // Act
        double regularPay = workLog.calculateRegularPay();

        // Assert
        assertEquals(200.0, regularPay); // 8 hours * $25/hour
    }

    @Test
    void calculateOvertimePay() {
        // Arrange
        workLog.setHoursWorked(10.0);

        // Act
        double overtimePay = workLog.calculateOvertimePay();

        // Assert
        assertEquals(75.0, overtimePay); // 2 overtime hours * $25/hour * 1.5
    }

    @Test
    void calculateTotalPay() {
        // Arrange
        workLog.setHoursWorked(10.0);

        // Act
        double totalPay = workLog.calculateTotalPay();

        // Assert
        assertEquals(275.0, totalPay); // $200 regular + $75 overtime
    }

    @Test
    void equalsAndHashCode() {
        // Arrange
        WorkLog sameWorkLog = WorkLog.builder()
                .id(1L)
                .user(user)
                .date(workLog.getDate())
                .build();
        WorkLog differentWorkLog = WorkLog.builder()
                .id(2L)
                .user(user)
                .date(workLog.getDate())
                .build();

        // Assert
        assertEquals(workLog, sameWorkLog);
        assertNotEquals(workLog, differentWorkLog);
        assertEquals(workLog.hashCode(), sameWorkLog.hashCode());
        assertNotEquals(workLog.hashCode(), differentWorkLog.hashCode());
    }

    @Test
    void toStringContainsImportantFields() {
        // Act
        String toString = workLog.toString();

        // Assert
        assertTrue(toString.contains(workLog.getId().toString()));
        assertTrue(toString.contains(workLog.getDate().toString()));
        assertTrue(toString.contains(String.valueOf(workLog.getHoursWorked())));
        assertTrue(toString.contains(workLog.getStatus().toString()));
    }

    @Test
    void statusTransitions() {
        // Initial status
        assertEquals(WorkLog.WorkLogStatus.PENDING, workLog.getStatus());

        // Approve
        workLog.setStatus(WorkLog.WorkLogStatus.APPROVED);
        assertEquals(WorkLog.WorkLogStatus.APPROVED, workLog.getStatus());

        // Reject
        workLog.setStatus(WorkLog.WorkLogStatus.REJECTED);
        assertEquals(WorkLog.WorkLogStatus.REJECTED, workLog.getStatus());
    }

    @Test
    void timestampsAreSetCorrectly() {
        // Assert
        assertNotNull(workLog.getCreatedAt());
        assertNotNull(workLog.getUpdatedAt());
        assertTrue(workLog.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(workLog.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void builderCreatesValidWorkLog() {
        // Act
        WorkLog builtWorkLog = WorkLog.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Test remarks")
                .status(WorkLog.WorkLogStatus.PENDING)
                .build();

        // Assert
        assertNotNull(builtWorkLog);
        assertEquals(8.0, builtWorkLog.getHoursWorked());
        assertEquals("Test remarks", builtWorkLog.getRemarks());
        assertEquals(WorkLog.WorkLogStatus.PENDING, builtWorkLog.getStatus());
    }
}
