package com.ems.dto.worklog;

import com.ems.model.WorkLog;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WorkLogDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    class WorkLogRequestTest {
        @Test
        void validWorkLogRequest() {
            // Arrange
            WorkLogRequest request = WorkLogRequest.builder()
                    .date(LocalDate.now())
                    .hoursWorked(8.0)
                    .remarks("Regular work day")
                    .build();

            // Act
            Set<ConstraintViolation<WorkLogRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        void invalidWorkLogRequestWithNullDate() {
            // Arrange
            WorkLogRequest request = WorkLogRequest.builder()
                    .date(null)
                    .hoursWorked(8.0)
                    .remarks("Regular work day")
                    .build();

            // Act
            Set<ConstraintViolation<WorkLogRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("date")));
        }

        @Test
        void invalidWorkLogRequestWithNegativeHours() {
            // Arrange
            WorkLogRequest request = WorkLogRequest.builder()
                    .date(LocalDate.now())
                    .hoursWorked(-1.0)
                    .remarks("Regular work day")
                    .build();

            // Act
            Set<ConstraintViolation<WorkLogRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("hoursWorked")));
        }

        @Test
        void invalidWorkLogRequestWithExcessiveHours() {
            // Arrange
            WorkLogRequest request = WorkLogRequest.builder()
                    .date(LocalDate.now())
                    .hoursWorked(25.0)
                    .remarks("Regular work day")
                    .build();

            // Act
            Set<ConstraintViolation<WorkLogRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("hoursWorked")));
        }
    }

    @Nested
    class WorkLogResponseTest {
        @Test
        void validWorkLogResponse() {
            // Arrange & Act
            WorkLogResponse response = WorkLogResponse.builder()
                    .id(1L)
                    .userId(1L)
                    .userName("Test User")
                    .date(LocalDate.now())
                    .hoursWorked(8.0)
                    .remarks("Regular work day")
                    .status(WorkLog.WorkLogStatus.PENDING)
                    .calculatedPay(200.0)
                    .build();

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(8.0, response.getHoursWorked());
            assertEquals(200.0, response.getCalculatedPay());
            assertEquals(WorkLog.WorkLogStatus.PENDING, response.getStatus());
        }

        @Test
        void workLogResponseEquality() {
            // Arrange
            WorkLogResponse response1 = WorkLogResponse.builder()
                    .id(1L)
                    .date(LocalDate.now())
                    .hoursWorked(8.0)
                    .build();
            WorkLogResponse response2 = WorkLogResponse.builder()
                    .id(1L)
                    .date(LocalDate.now())
                    .hoursWorked(8.0)
                    .build();

            // Assert
            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        void workLogResponseToString() {
            // Arrange
            LocalDate date = LocalDate.now();
            WorkLogResponse response = WorkLogResponse.builder()
                    .id(1L)
                    .userId(1L)
                    .userName("Test User")
                    .date(date)
                    .hoursWorked(8.0)
                    .remarks("Regular work day")
                    .status(WorkLog.WorkLogStatus.PENDING)
                    .calculatedPay(200.0)
                    .build();

            // Act
            String toString = response.toString();

            // Assert
            assertTrue(toString.contains("1")); // ID
            assertTrue(toString.contains("Test User")); // userName
            assertTrue(toString.contains(date.toString())); // date
            assertTrue(toString.contains("8.0")); // hoursWorked
            assertTrue(toString.contains("200.0")); // calculatedPay
            assertTrue(toString.contains("PENDING")); // status
        }

        @Test
        void workLogResponseBuilder() {
            // Arrange
            LocalDate date = LocalDate.now();

            // Act
            WorkLogResponse response = WorkLogResponse.builder()
                    .id(1L)
                    .userId(1L)
                    .userName("Test User")
                    .date(date)
                    .hoursWorked(8.0)
                    .remarks("Regular work day")
                    .status(WorkLog.WorkLogStatus.PENDING)
                    .calculatedPay(200.0)
                    .build();

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(1L, response.getUserId());
            assertEquals("Test User", response.getUserName());
            assertEquals(date, response.getDate());
            assertEquals(8.0, response.getHoursWorked());
            assertEquals("Regular work day", response.getRemarks());
            assertEquals(WorkLog.WorkLogStatus.PENDING, response.getStatus());
            assertEquals(200.0, response.getCalculatedPay());
        }
    }
}
