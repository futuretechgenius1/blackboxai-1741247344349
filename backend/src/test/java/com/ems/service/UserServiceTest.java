package com.ems.service;

import com.ems.exception.ResourceNotFoundException;
import com.ems.exception.UnauthorizedAccessException;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.repository.UserRepository;
import com.ems.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User employeeUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        adminUser = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("admin")
                .asAdmin()
                .build();

        employeeUser = TestUtil.UserBuilder.aUser()
                .withId(2L)
                .withUsername("employee")
                .build();

        testUser = TestUtil.UserBuilder.aUser()
                .withId(3L)
                .withUsername("test.user")
                .build();
    }

    @Test
    void getAllUsersAsAdmin() {
        // Arrange
        List<User> users = Arrays.asList(adminUser, employeeUser);
        Page<User> userPage = new PageImpl<>(users);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<User> result = userService.getAllUsers(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsersAsEmployee() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(employeeUser);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> userService.getAllUsers(Pageable.unpaged()));
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getUserByIdSuccess() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void getUserByIdNotFound() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    @Test
    void updateUserAsAdmin() {
        // Arrange
        User updatedUser = TestUtil.UserBuilder.aUser()
                .withId(testUser.getId())
                .withUsername("updated.user")
                .build();

        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(testUser.getId(), updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(updatedUser.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserUnauthorized() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(employeeUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> userService.updateUser(testUser.getId(), testUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserAsAdmin() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, employeeUser, testUser));

        // Act
        userService.deleteUser(testUser.getId());

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteLastAdminUser() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, employeeUser));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> userService.deleteUser(adminUser.getId()));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateUserStatusAsAdmin() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUserStatus(testUser.getId(), false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void disableLastActiveAdmin() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, employeeUser));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> userService.updateUserStatus(adminUser.getId(), false));
        verify(userRepository, never()).save(any(User.class));
    }
}
