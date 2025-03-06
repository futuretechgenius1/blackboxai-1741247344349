package com.ems.util;

import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.RegisterRequest;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.ems.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Utility class containing helper methods for tests
 */
public final class TestUtils {

    private static final Random RANDOM = new Random();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestUtils() {
        // Private constructor to prevent instantiation
    }

    // Authentication utilities
    public static void setAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    // Request building utilities
    public static ResultActions performGet(MockMvc mockMvc, String url, String token) throws Exception {
        MockHttpServletRequestBuilder request = get(url);
        if (token != null) {
            request.header(AUTHORIZATION_HEADER, JWT_TOKEN_PREFIX + token);
        }
        return mockMvc.perform(request);
    }

    public static ResultActions performPost(MockMvc mockMvc, String url, Object content, String token) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(content));
        if (token != null) {
            request.header(AUTHORIZATION_HEADER, JWT_TOKEN_PREFIX + token);
        }
        return mockMvc.perform(request);
    }

    public static ResultActions performPut(MockMvc mockMvc, String url, Object content, String token) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(content));
        if (token != null) {
            request.header(AUTHORIZATION_HEADER, JWT_TOKEN_PREFIX + token);
        }
        return mockMvc.perform(request);
    }

    public static ResultActions performDelete(MockMvc mockMvc, String url, String token) throws Exception {
        MockHttpServletRequestBuilder request = delete(url);
        if (token != null) {
            request.header(AUTHORIZATION_HEADER, JWT_TOKEN_PREFIX + token);
        }
        return mockMvc.perform(request);
    }

    // Data generation utilities
    public static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String generateRandomEmail() {
        return "test." + generateRandomString(8) + "@ems.com";
    }

    public static double generateRandomHours(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    public static LocalDate generateRandomDate(LocalDate start, LocalDate end) {
        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();
        long randomDay = startEpochDay + RANDOM.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomDay);
    }

    // Test data creation utilities
    public static List<WorkLog> createTestWorkLogs(User user, int count) {
        List<WorkLog> workLogs = new ArrayList<>();
        LocalDate date = LocalDate.now();
        for (int i = 0; i < count; i++) {
            workLogs.add(WorkLog.builder()
                .id((long) (i + 1))
                .user(user)
                .date(date.minusDays(i))
                .hoursWorked(DEFAULT_WORK_HOURS)
                .remarks(DEFAULT_REMARKS)
                .status(WorkLog.WorkLogStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        }
        return workLogs;
    }

    public static List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.builder()
                .id((long) (i + 1))
                .username("test.user." + i)
                .email("test.user." + i + "@ems.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User " + i)
                .role(Role.ROLE_EMPLOYEE)
                .department(DEFAULT_DEPARTMENT)
                .position(DEFAULT_POSITION)
                .hourlyRate(DEFAULT_HOURLY_RATE)
                .enabled(true)
                .build());
        }
        return users;
    }

    // Request/Response building utilities
    public static AuthRequest createAuthRequest(String username, String password) {
        return AuthRequest.builder()
            .username(username)
            .password(password)
            .build();
    }

    public static RegisterRequest createRegisterRequest() {
        String random = generateRandomString(8);
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

    // Date formatting utilities
    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    // Validation utilities
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9._-]{3,50}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    }
}
