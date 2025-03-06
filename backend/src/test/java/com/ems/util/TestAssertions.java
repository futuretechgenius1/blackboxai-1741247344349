package com.ems.util;

import com.ems.dto.MonthlyWorkSummary;
import com.ems.dto.auth.AuthResponse;
import com.ems.dto.worklog.WorkLogResponse;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Custom assertions for testing EMS application components
 */
public final class TestAssertions {

    private TestAssertions() {
        // Private constructor to prevent instantiation
    }

    // User assertions
    public static void assertUser(User actual, User expected) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
        assertEquals(expected.getHourlyRate(), actual.getHourlyRate());
        assertEquals(expected.isEnabled(), actual.isEnabled());
    }

    public static void assertUserResponse(ResultActions result, User expected) throws Exception {
        result.andExpect(jsonPath("$.id").value(expected.getId()))
             .andExpect(jsonPath("$.username").value(expected.getUsername()))
             .andExpect(jsonPath("$.email").value(expected.getEmail()))
             .andExpect(jsonPath("$.firstName").value(expected.getFirstName()))
             .andExpect(jsonPath("$.lastName").value(expected.getLastName()))
             .andExpect(jsonPath("$.role").value(expected.getRole().name()))
             .andExpect(jsonPath("$.department").value(expected.getDepartment()))
             .andExpect(jsonPath("$.position").value(expected.getPosition()))
             .andExpect(jsonPath("$.hourlyRate").value(expected.getHourlyRate()))
             .andExpect(jsonPath("$.enabled").value(expected.isEnabled()));
    }

    // WorkLog assertions
    public static void assertWorkLog(WorkLog actual, WorkLog expected) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUser().getId(), actual.getUser().getId());
        assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getHoursWorked(), actual.getHoursWorked());
        assertEquals(expected.getRemarks(), actual.getRemarks());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    public static void assertWorkLogResponse(ResultActions result, WorkLogResponse expected) throws Exception {
        result.andExpect(jsonPath("$.id").value(expected.getId()))
             .andExpect(jsonPath("$.userId").value(expected.getUserId()))
             .andExpect(jsonPath("$.userName").value(expected.getUserName()))
             .andExpect(jsonPath("$.date").value(expected.getDate().toString()))
             .andExpect(jsonPath("$.hoursWorked").value(expected.getHoursWorked()))
             .andExpect(jsonPath("$.remarks").value(expected.getRemarks()))
             .andExpect(jsonPath("$.status").value(expected.getStatus().name()))
             .andExpect(jsonPath("$.calculatedPay").value(expected.getCalculatedPay()));
    }

    // Authentication assertions
    public static void assertAuthResponse(ResultActions result, AuthResponse expected) throws Exception {
        result.andExpect(jsonPath("$.token").exists())
             .andExpect(jsonPath("$.username").value(expected.getUsername()))
             .andExpect(jsonPath("$.email").value(expected.getEmail()))
             .andExpect(jsonPath("$.role").value(expected.getRole().name()));
    }

    // MonthlyWorkSummary assertions
    public static void assertMonthlyWorkSummary(MonthlyWorkSummary actual, MonthlyWorkSummary expected) {
        assertNotNull(actual);
        assertEquals(expected.getYearMonth(), actual.getYearMonth());
        assertEquals(expected.getTotalHoursWorked(), actual.getTotalHoursWorked());
        assertEquals(expected.getApprovedHours(), actual.getApprovedHours());
        assertEquals(expected.getPendingHours(), actual.getPendingHours());
        assertEquals(expected.getWorkDaysCount(), actual.getWorkDaysCount());
    }

    // List assertions
    public static void assertPageResponse(ResultActions result, int totalElements, int pageSize) throws Exception {
        result.andExpect(jsonPath("$.content").exists())
             .andExpect(jsonPath("$.totalElements").value(totalElements))
             .andExpect(jsonPath("$.size").value(pageSize));
    }

    // Role assertions
    public static void assertRole(Role actual, Role expected) {
        assertEquals(expected, actual);
    }

    // Date assertions
    public static void assertDateEquals(LocalDate actual, LocalDate expected) {
        assertEquals(expected, actual);
    }

    public static void assertDateTimeEquals(LocalDateTime actual, LocalDateTime expected) {
        assertEquals(expected, actual);
    }

    // Error response assertions
    public static void assertErrorResponse(ResultActions result, String expectedMessage) throws Exception {
        result.andExpect(jsonPath("$.message").value(expectedMessage));
    }

    public static void assertValidationError(ResultActions result, String field) throws Exception {
        result.andExpect(jsonPath("$.errors." + field).exists());
    }

    // Custom matchers
    public static ResultMatcher hasValidJwtToken() {
        return jsonPath("$.token", matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"));
    }

    public static ResultMatcher hasValidTimestamp() {
        return jsonPath("$.timestamp", matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    // Collection assertions
    public static void assertWorkLogList(List<WorkLog> actual, List<WorkLog> expected) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertWorkLog(actual.get(i), expected.get(i));
        }
    }

    public static void assertUserList(List<User> actual, List<User> expected) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertUser(actual.get(i), expected.get(i));
        }
    }

    // Numeric assertions
    public static void assertMoneyEquals(double expected, double actual) {
        assertEquals(expected, actual, 0.01);
    }

    public static void assertHoursEquals(double expected, double actual) {
        assertEquals(expected, actual, 0.01);
    }
}
