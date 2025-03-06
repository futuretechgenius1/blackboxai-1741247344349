package com.ems;

import java.time.format.DateTimeFormatter;

/**
 * Constants used across test classes
 */
public final class TestConstants {

    private TestConstants() {
        // Private constructor to prevent instantiation
    }

    // API Endpoints
    public static final String API_BASE_PATH = "/api";
    public static final String AUTH_BASE_PATH = API_BASE_PATH + "/auth";
    public static final String USERS_BASE_PATH = API_BASE_PATH + "/users";
    public static final String WORK_LOGS_BASE_PATH = API_BASE_PATH + "/work-logs";
    public static final String PAYROLL_BASE_PATH = API_BASE_PATH + "/payroll";

    // Authentication endpoints
    public static final String LOGIN_ENDPOINT = AUTH_BASE_PATH + "/login";
    public static final String REGISTER_ENDPOINT = AUTH_BASE_PATH + "/register";
    public static final String VALIDATE_TOKEN_ENDPOINT = AUTH_BASE_PATH + "/validate";

    // User credentials
    public static final String TEST_ADMIN_USERNAME = "admin.test";
    public static final String TEST_ADMIN_PASSWORD = "Admin@123";
    public static final String TEST_ADMIN_EMAIL = "admin.test@ems.com";
    public static final String TEST_EMPLOYEE_USERNAME = "employee.test";
    public static final String TEST_EMPLOYEE_PASSWORD = "Employee@123";
    public static final String TEST_EMPLOYEE_EMAIL = "employee.test@ems.com";

    // Test data
    public static final Double DEFAULT_HOURLY_RATE = 25.0;
    public static final Double DEFAULT_WORK_HOURS = 8.0;
    public static final String DEFAULT_DEPARTMENT = "IT";
    public static final String DEFAULT_POSITION = "Software Engineer";
    public static final String DEFAULT_REMARKS = "Regular work day";

    // Date formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    // JWT related
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TEST_JWT_SECRET = "testSecretKeyWithAtLeast32CharactersForTesting";
    public static final long JWT_EXPIRATION = 3600000; // 1 hour in milliseconds

    // Test data sizes
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // Validation constants
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_NAME_LENGTH = 50;
    public static final double MIN_HOURLY_RATE = 0.0;
    public static final double MAX_HOURLY_RATE = 1000.0;
    public static final double MIN_HOURS_WORKED = 0.0;
    public static final double MAX_HOURS_WORKED = 24.0;

    // Error messages
    public static final String ERROR_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_FORBIDDEN = "Access Denied";
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_BAD_REQUEST = "Invalid request";
    public static final String ERROR_VALIDATION = "Validation failed";
    public static final String ERROR_DUPLICATE = "Resource already exists";

    // Test file paths
    public static final String TEST_RESOURCES_PATH = "src/test/resources";
    public static final String TEST_FILES_PATH = TEST_RESOURCES_PATH + "/files";
    public static final String TEST_REPORTS_PATH = TEST_RESOURCES_PATH + "/reports";

    // Content types
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String TEXT_CSV = "text/csv";
    public static final String TEXT_PLAIN = "text/plain";

    // Test timeouts
    public static final long DEFAULT_TIMEOUT = 5000L;
    public static final long EXTENDED_TIMEOUT = 10000L;

    // Test database
    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    public static final String TEST_DB_USERNAME = "sa";
    public static final String TEST_DB_PASSWORD = "";

    // Test server
    public static final String TEST_SERVER_HOST = "localhost";
    public static final int TEST_SERVER_PORT = 8081;
    public static final String TEST_SERVER_URL = "http://" + TEST_SERVER_HOST + ":" + TEST_SERVER_PORT;

    // CORS settings
    public static final String ALLOWED_ORIGIN = "http://localhost:3000";
    public static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
    public static final String[] ALLOWED_HEADERS = {"Authorization", "Content-Type"};
    public static final long MAX_AGE = 3600L;

    // Regex patterns
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{3,50}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
}
