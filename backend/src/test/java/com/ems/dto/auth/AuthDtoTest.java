package com.ems.dto.auth;

import com.ems.model.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    class AuthRequestTest {
        @Test
        void validAuthRequest() {
            // Arrange
            AuthRequest request = AuthRequest.builder()
                    .username("testuser")
                    .password("Test@123")
                    .build();

            // Act
            Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        void invalidAuthRequestWithEmptyUsername() {
            // Arrange
            AuthRequest request = AuthRequest.builder()
                    .username("")
                    .password("Test@123")
                    .build();

            // Act
            Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        }

        @Test
        void invalidAuthRequestWithEmptyPassword() {
            // Arrange
            AuthRequest request = AuthRequest.builder()
                    .username("testuser")
                    .password("")
                    .build();

            // Act
            Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        }
    }

    @Nested
    class AuthResponseTest {
        @Test
        void validAuthResponse() {
            // Arrange & Act
            AuthResponse response = AuthResponse.builder()
                    .token("jwt.token.here")
                    .username("testuser")
                    .email("test@example.com")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.ROLE_EMPLOYEE)
                    .message("Authentication successful")
                    .build();

            // Assert
            assertNotNull(response);
            assertEquals("jwt.token.here", response.getToken());
            assertEquals("testuser", response.getUsername());
            assertEquals("test@example.com", response.getEmail());
            assertEquals(Role.ROLE_EMPLOYEE, response.getRole());
        }

        @Test
        void authResponseEquality() {
            // Arrange
            AuthResponse response1 = AuthResponse.builder()
                    .token("token")
                    .username("user")
                    .build();
            AuthResponse response2 = AuthResponse.builder()
                    .token("token")
                    .username("user")
                    .build();

            // Assert
            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }
    }

    @Nested
    class RegisterRequestTest {
        @Test
        void validRegisterRequest() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("Test@123")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.ROLE_EMPLOYEE)
                    .department("IT")
                    .position("Developer")
                    .hourlyRate(25.0)
                    .build();

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        void invalidRegisterRequestWithInvalidEmail() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("invalid-email")
                    .password("Test@123")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.ROLE_EMPLOYEE)
                    .build();

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        void invalidRegisterRequestWithNegativeHourlyRate() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("Test@123")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.ROLE_EMPLOYEE)
                    .hourlyRate(-10.0)
                    .build();

            // Act
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("hourlyRate")));
        }

        @Test
        void registerRequestToString() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("Test@123")
                    .build();

            // Act
            String toString = request.toString();

            // Assert
            assertTrue(toString.contains("testuser"));
            assertTrue(toString.contains("test@example.com"));
            assertFalse(toString.contains("Test@123")); // Password should not be in toString
        }
    }
}
