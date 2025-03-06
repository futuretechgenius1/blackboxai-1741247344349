package com.ems.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = User.builder()
                .id(1L)
                .username("test.user")
                .email("test.user@example.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .department("IT")
                .position("Software Engineer")
                .hourlyRate(25.0)
                .enabled(true)
                .build();
    }

    @Test
    void validUser() {
        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidUserWithEmptyUsername() {
        // Arrange
        user.setUsername("");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void invalidUserWithInvalidEmail() {
        // Arrange
        user.setEmail("invalid-email");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void invalidUserWithNegativeHourlyRate() {
        // Arrange
        user.setHourlyRate(-10.0);

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("hourlyRate")));
    }

    @Test
    void getAuthoritiesReturnsCorrectRole() {
        // Act
        var authorities = user.getAuthorities();

        // Assert
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Role.ROLE_EMPLOYEE.name())));
    }

    @Test
    void getFullNameReturnsCorrectFormat() {
        // Act
        String fullName = user.getFullName();

        // Assert
        assertEquals("Test User", fullName);
    }

    @Test
    void userAccountNonExpiredByDefault() {
        // Assert
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void userAccountNonLockedByDefault() {
        // Assert
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void userCredentialsNonExpiredByDefault() {
        // Assert
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void userEnabledByDefault() {
        // Assert
        assertTrue(user.isEnabled());
    }

    @Test
    void equalsAndHashCode() {
        // Arrange
        User sameUser = User.builder()
                .id(1L)
                .username("test.user")
                .build();
        User differentUser = User.builder()
                .id(2L)
                .username("other.user")
                .build();

        // Assert
        assertEquals(user, sameUser);
        assertNotEquals(user, differentUser);
        assertEquals(user.hashCode(), sameUser.hashCode());
        assertNotEquals(user.hashCode(), differentUser.hashCode());
    }

    @Test
    void toStringContainsImportantFields() {
        // Act
        String toString = user.toString();

        // Assert
        assertTrue(toString.contains(user.getId().toString()));
        assertTrue(toString.contains(user.getUsername()));
        assertTrue(toString.contains(user.getEmail()));
        assertFalse(toString.contains(user.getPassword())); // Password should not be included
    }

    @Test
    void builderCreatesValidUser() {
        // Act
        User builtUser = User.builder()
                .id(1L)
                .username("test.user")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .build();

        // Assert
        assertNotNull(builtUser);
        assertEquals("test.user", builtUser.getUsername());
        assertEquals("test@example.com", builtUser.getEmail());
        assertEquals(Role.ROLE_EMPLOYEE, builtUser.getRole());
    }

    @Test
    void userRoleHasCorrectAuthority() {
        // Arrange
        user.setRole(Role.ROLE_ADMIN);

        // Act
        var authorities = user.getAuthorities();

        // Assert
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name())));
    }

    @Test
    void settersUpdateFieldsCorrectly() {
        // Act
        user.setUsername("updated.user");
        user.setEmail("updated@example.com");
        user.setRole(Role.ROLE_ADMIN);

        // Assert
        assertEquals("updated.user", user.getUsername());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
    }
}
