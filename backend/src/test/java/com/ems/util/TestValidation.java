package com.ems.util;

import com.ems.dto.MonthlyWorkSummary;
import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.RegisterRequest;
import com.ems.dto.worklog.WorkLogRequest;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Utility class for test data validation
 */
public final class TestValidation {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private TestValidation() {
        // Private constructor to prevent instantiation
    }

    /**
     * User validation methods
     */
    public static void validateUser(User user) {
        assertNotNull(user, "User should not be null");
        assertNotNull(user.getId(), "User ID should not be null");
        assertTrue(isValidUsername(user.getUsername()), "Invalid username format");
        assertTrue(isValidEmail(user.getEmail()), "Invalid email format");
        assertNotNull(user.getRole(), "User role should not be null");
        assertTrue(user.getHourlyRate() >= 0, "Hourly rate should not be negative");
    }

    public static void validateUserAuthorities(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities, "User authorities should not be null");
        assertFalse(authorities.isEmpty(), "User should have at least one authority");
        assertTrue(authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals(user.getRole().name())),
            "User should have authority matching their role");
    }

    /**
     * WorkLog validation methods
     */
    public static void validateWorkLog(WorkLog workLog) {
        assertNotNull(workLog, "WorkLog should not be null");
        assertNotNull(workLog.getId(), "WorkLog ID should not be null");
        assertNotNull(workLog.getUser(), "WorkLog user should not be null");
        assertNotNull(workLog.getDate(), "WorkLog date should not be null");
        assertTrue(workLog.getHoursWorked() > 0, "Hours worked should be positive");
        assertTrue(workLog.getHoursWorked() <= 24, "Hours worked should not exceed 24");
        assertNotNull(workLog.getStatus(), "WorkLog status should not be null");
    }

    public static void validateWorkLogCalculations(WorkLog workLog) {
        double regularHours = workLog.getRegularHours();
        double overtimeHours = workLog.getOvertimeHours();
        double totalHours = workLog.getHoursWorked();

        assertTrue(regularHours >= 0, "Regular hours should not be negative");
        assertTrue(overtimeHours >= 0, "Overtime hours should not be negative");
        assertEquals(totalHours, regularHours + overtimeHours, 
            "Total hours should equal regular plus overtime hours");
    }

    /**
     * Request validation methods
     */
    public static void validateAuthRequest(AuthRequest request) {
        assertNotNull(request, "Auth request should not be null");
        assertTrue(isValidUsername(request.getUsername()), "Invalid username format");
        assertNotNull(request.getPassword(), "Password should not be null");
    }

    public static void validateRegisterRequest(RegisterRequest request) {
        assertNotNull(request, "Register request should not be null");
        assertTrue(isValidUsername(request.getUsername()), "Invalid username format");
        assertTrue(isValidEmail(request.getEmail()), "Invalid email format");
        assertTrue(isValidPassword(request.getPassword()), "Invalid password format");
        assertNotNull(request.getRole(), "Role should not be null");
    }

    public static void validateWorkLogRequest(WorkLogRequest request) {
        assertNotNull(request, "WorkLog request should not be null");
        assertNotNull(request.getDate(), "Date should not be null");
        assertTrue(request.getHoursWorked() > 0, "Hours worked should be positive");
        assertTrue(request.getHoursWorked() <= 24, "Hours worked should not exceed 24");
    }

    /**
     * Response validation methods
     */
    public static void validateSuccessResponse(ResultActions result) throws Exception {
        result.andExpect(status().is2xxSuccessful());
    }

    public static void validateErrorResponse(ResultActions result, HttpStatus expectedStatus) 
            throws Exception {
        result.andExpect(status().is(expectedStatus.value()));
    }

    /**
     * Data format validation methods
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Date validation methods
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        assertNotNull(startDate, "Start date should not be null");
        assertNotNull(endDate, "End date should not be null");
        assertTrue(startDate.isBefore(endDate) || startDate.isEqual(endDate),
            "Start date should be before or equal to end date");
    }

    public static void validateDateTime(LocalDateTime dateTime) {
        assertNotNull(dateTime, "DateTime should not be null");
        assertTrue(dateTime.isBefore(LocalDateTime.now().plusSeconds(1)),
            "DateTime should not be in the future");
    }

    /**
     * Summary validation methods
     */
    public static void validateMonthlyWorkSummary(MonthlyWorkSummary summary) {
        assertNotNull(summary, "Monthly work summary should not be null");
        assertNotNull(summary.getYearMonth(), "Year month should not be null");
        assertTrue(summary.getTotalHoursWorked() >= 0, "Total hours should not be negative");
        assertTrue(summary.getWorkDaysCount() >= 0, "Work days count should not be negative");
    }

    /**
     * Collection validation methods
     */
    public static void validateList(List<?> list, int expectedSize) {
        assertNotNull(list, "List should not be null");
        assertEquals(expectedSize, list.size(), 
            "List size should match expected size");
    }

    public static void validateNonEmptyList(List<?> list) {
        assertNotNull(list, "List should not be null");
        assertFalse(list.isEmpty(), "List should not be empty");
    }

    /**
     * Role validation methods
     */
    public static void validateRole(Role role) {
        assertNotNull(role, "Role should not be null");
        assertTrue(role == Role.ROLE_ADMIN || role == Role.ROLE_EMPLOYEE,
            "Role should be either ADMIN or EMPLOYEE");
    }

    /**
     * Numeric validation methods
     */
    public static void validatePositiveNumber(double number) {
        assertTrue(number > 0, "Number should be positive");
    }

    public static void validateNonNegativeNumber(double number) {
        assertTrue(number >= 0, "Number should not be negative");
    }

    public static void validatePercentage(double percentage) {
        assertTrue(percentage >= 0 && percentage <= 100,
            "Percentage should be between 0 and 100");
    }

    /**
     * Time period validation methods
     */
    public static void validateYearMonth(YearMonth yearMonth) {
        assertNotNull(yearMonth, "YearMonth should not be null");
        assertTrue(yearMonth.isBefore(YearMonth.now().plusMonths(1)),
            "YearMonth should not be in the future");
    }
}
