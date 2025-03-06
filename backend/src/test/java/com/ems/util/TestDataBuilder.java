package com.ems.util;

import com.ems.dto.MonthlyWorkSummary;
import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.AuthResponse;
import com.ems.dto.auth.RegisterRequest;
import com.ems.dto.worklog.WorkLogRequest;
import com.ems.dto.worklog.WorkLogResponse;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ems.TestConstants.*;

/**
 * Utility class for building test data
 */
public final class TestDataBuilder {

    private TestDataBuilder() {
        // Private constructor to prevent instantiation
    }

    // User builders
    public static User buildTestAdmin() {
        return User.builder()
                .id(1L)
                .username(TEST_ADMIN_USERNAME)
                .email(TEST_ADMIN_EMAIL)
                .password(TEST_ADMIN_PASSWORD)
                .firstName("Admin")
                .lastName("Test")
                .role(Role.ROLE_ADMIN)
                .department(DEFAULT_DEPARTMENT)
                .position("System Administrator")
                .hourlyRate(DEFAULT_HOURLY_RATE)
                .enabled(true)
                .build();
    }

    public static User buildTestEmployee() {
        return User.builder()
                .id(2L)
                .username(TEST_EMPLOYEE_USERNAME)
                .email(TEST_EMPLOYEE_EMAIL)
                .password(TEST_EMPLOYEE_PASSWORD)
                .firstName("Employee")
                .lastName("Test")
                .role(Role.ROLE_EMPLOYEE)
                .department(DEFAULT_DEPARTMENT)
                .position(DEFAULT_POSITION)
                .hourlyRate(DEFAULT_HOURLY_RATE)
                .enabled(true)
                .build();
    }

    public static User buildRandomUser() {
        String random = UUID.randomUUID().toString().substring(0, 8);
        return User.builder()
                .username("user." + random)
                .email("user." + random + "@ems.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .department(DEFAULT_DEPARTMENT)
                .position(DEFAULT_POSITION)
                .hourlyRate(DEFAULT_HOURLY_RATE)
                .enabled(true)
                .build();
    }

    // WorkLog builders
    public static WorkLog buildTestWorkLog(User user) {
        return WorkLog.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .hoursWorked(DEFAULT_WORK_HOURS)
                .remarks(DEFAULT_REMARKS)
                .status(WorkLog.WorkLogStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static List<WorkLog> buildTestWorkLogs(User user, int count) {
        List<WorkLog> workLogs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            workLogs.add(WorkLog.builder()
                    .id((long) (i + 1))
                    .user(user)
                    .date(LocalDate.now().minusDays(i))
                    .hoursWorked(DEFAULT_WORK_HOURS)
                    .remarks(DEFAULT_REMARKS + " " + (i + 1))
                    .status(WorkLog.WorkLogStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        return workLogs;
    }

    // DTO builders
    public static RegisterRequest buildRegisterRequest() {
        String random = UUID.randomUUID().toString().substring(0, 8);
        return RegisterRequest.builder()
                .username("test.user." + random)
                .email("test.user." + random + "@ems.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .department(DEFAULT_DEPARTMENT)
                .position(DEFAULT_POSITION)
                .hourlyRate(DEFAULT_HOURLY_RATE)
                .build();
    }

    public static AuthRequest buildAuthRequest(String username) {
        return AuthRequest.builder()
                .username(username)
                .password("Test@123")
                .build();
    }

    public static AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token("test.jwt.token")
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .message("Authentication successful")
                .build();
    }

    public static WorkLogRequest buildWorkLogRequest() {
        return WorkLogRequest.builder()
                .date(LocalDate.now())
                .hoursWorked(DEFAULT_WORK_HOURS)
                .remarks(DEFAULT_REMARKS)
                .build();
    }

    public static WorkLogResponse buildWorkLogResponse(WorkLog workLog) {
        return WorkLogResponse.builder()
                .id(workLog.getId())
                .userId(workLog.getUser().getId())
                .userName(workLog.getUser().getFullName())
                .date(workLog.getDate())
                .hoursWorked(workLog.getHoursWorked())
                .remarks(workLog.getRemarks())
                .status(workLog.getStatus())
                .calculatedPay(workLog.calculateTotalPay())
                .build();
    }

    public static MonthlyWorkSummary buildMonthlyWorkSummary(List<WorkLog> workLogs) {
        return new MonthlyWorkSummary(YearMonth.now(), workLogs);
    }

    // Error response builders
    public static String buildErrorResponse(String message) {
        return String.format("{\"message\":\"%s\",\"timestamp\":\"%s\"}", 
                message, LocalDateTime.now().format(DATETIME_FORMATTER));
    }

    // JWT Token builders
    public static String buildJwtToken(User user) {
        return "test.jwt.token.for." + user.getUsername();
    }

    public static String buildAuthorizationHeader(User user) {
        return JWT_TOKEN_PREFIX + buildJwtToken(user);
    }

    // Validation builders
    public static String buildValidationError(String field, String message) {
        return String.format("{\"errors\":{\"%s\":\"%s\"}}", field, message);
    }
}
