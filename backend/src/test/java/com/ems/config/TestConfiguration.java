package com.ems.config;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@TestConfiguration
@PropertySource("classpath:test.properties")
public class TestConfiguration {

    @Value("${test.admin.username}")
    private String adminUsername;

    @Value("${test.admin.password}")
    private String adminPassword;

    @Value("${test.admin.email}")
    private String adminEmail;

    @Value("${test.employee.username}")
    private String employeeUsername;

    @Value("${test.employee.password}")
    private String employeePassword;

    @Value("${test.employee.email}")
    private String employeeEmail;

    @Value("${test.data.worklog.hours}")
    private Double defaultWorkHours;

    @Value("${test.data.hourly.rate}")
    private Double defaultHourlyRate;

    @Value("${test.data.department}")
    private String defaultDepartment;

    @Value("${test.data.position}")
    private String defaultPosition;

    @Value("${test.date.format}")
    private String dateFormat;

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public User testAdminUser() {
        return User.builder()
                .id(1L)
                .username(adminUsername)
                .password(testPasswordEncoder().encode(adminPassword))
                .email(adminEmail)
                .firstName("Admin")
                .lastName("Test")
                .role(Role.ROLE_ADMIN)
                .department(defaultDepartment)
                .position("System Administrator")
                .hourlyRate(defaultHourlyRate)
                .enabled(true)
                .build();
    }

    @Bean
    public User testEmployeeUser() {
        return User.builder()
                .id(2L)
                .username(employeeUsername)
                .password(testPasswordEncoder().encode(employeePassword))
                .email(employeeEmail)
                .firstName("Employee")
                .lastName("Test")
                .role(Role.ROLE_EMPLOYEE)
                .department(defaultDepartment)
                .position(defaultPosition)
                .hourlyRate(defaultHourlyRate)
                .enabled(true)
                .build();
    }

    @Bean
    public WorkLog testWorkLog() {
        return WorkLog.builder()
                .id(1L)
                .user(testEmployeeUser())
                .date(LocalDate.now())
                .hoursWorked(defaultWorkHours)
                .remarks("Test work log")
                .status(WorkLog.WorkLogStatus.PENDING)
                .build();
    }

    @Bean
    public DateTimeFormatter testDateFormatter() {
        return DateTimeFormatter.ofPattern(dateFormat);
    }

    // Utility methods for tests
    public String getTestJwtToken(User user) {
        // This would be implemented to generate a test JWT token
        return "test.jwt.token";
    }

    public String getTestAuthorizationHeader(User user) {
        return "Bearer " + getTestJwtToken(user);
    }

    public WorkLog createTestWorkLog(User user, LocalDate date, Double hours, WorkLog.WorkLogStatus status) {
        return WorkLog.builder()
                .user(user)
                .date(date)
                .hoursWorked(hours)
                .remarks("Test work log")
                .status(status)
                .build();
    }

    public User createTestUser(String username, String email, Role role) {
        return User.builder()
                .username(username)
                .email(email)
                .password(testPasswordEncoder().encode("Test@123"))
                .firstName("Test")
                .lastName("User")
                .role(role)
                .department(defaultDepartment)
                .position(defaultPosition)
                .hourlyRate(defaultHourlyRate)
                .enabled(true)
                .build();
    }

    // Getters for test properties
    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public String getEmployeePassword() {
        return employeePassword;
    }

    public Double getDefaultWorkHours() {
        return defaultWorkHours;
    }

    public Double getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public String getDefaultDepartment() {
        return defaultDepartment;
    }

    public String getDefaultPosition() {
        return defaultPosition;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
