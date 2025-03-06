package com.ems.config;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.ems.repository.UserRepository;
import com.ems.repository.WorkLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DatabaseSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkLogRepository workLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DatabaseSeeder databaseSeeder;

    @Captor
    private ArgumentCaptor<List<User>> usersCaptor;

    @Captor
    private ArgumentCaptor<List<WorkLog>> workLogsCaptor;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
    }

    @Test
    void shouldNotSeedWhenUsersExist() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(1L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(userRepository, never()).saveAll(any());
        verify(workLogRepository, never()).saveAll(any());
    }

    @Test
    void shouldSeedWhenDatabaseIsEmpty() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(userRepository).saveAll(usersCaptor.capture());
        verify(workLogRepository, atLeastOnce()).saveAll(workLogsCaptor.capture());

        List<User> seededUsers = usersCaptor.getValue();
        assertFalse(seededUsers.isEmpty());
        
        // Verify admin user
        User adminUser = seededUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .findFirst()
                .orElseThrow();
        assertEquals("admin", adminUser.getUsername());
        assertEquals("System", adminUser.getFirstName());
        assertEquals("Administrator", adminUser.getLastName());
        assertTrue(adminUser.isEnabled());

        // Verify employee users
        List<User> employees = seededUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_EMPLOYEE)
                .toList();
        assertFalse(employees.isEmpty());
    }

    @Test
    void shouldCreateWorkLogsForEmployees() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(workLogRepository, atLeastOnce()).saveAll(workLogsCaptor.capture());
        List<WorkLog> seededWorkLogs = workLogsCaptor.getValue();
        
        assertFalse(seededWorkLogs.isEmpty());
        seededWorkLogs.forEach(workLog -> {
            assertNotNull(workLog.getUser());
            assertNotNull(workLog.getDate());
            assertNotNull(workLog.getHoursWorked());
            assertNotNull(workLog.getStatus());
        });
    }

    @Test
    void shouldEncodePasswords() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(passwordEncoder, atLeast(3)).encode(any()); // At least admin and 2 employees
    }

    @Test
    void shouldCreateUsersWithCorrectRoles() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(userRepository).saveAll(usersCaptor.capture());
        List<User> seededUsers = usersCaptor.getValue();

        long adminCount = seededUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                .count();
        long employeeCount = seededUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_EMPLOYEE)
                .count();

        assertEquals(1, adminCount);
        assertTrue(employeeCount >= 2);
    }

    @Test
    void shouldCreateWorkLogsWithValidDates() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(workLogRepository, atLeastOnce()).saveAll(workLogsCaptor.capture());
        List<WorkLog> seededWorkLogs = workLogsCaptor.getValue();

        seededWorkLogs.forEach(workLog -> {
            assertNotNull(workLog.getDate());
            assertTrue(workLog.getDate().isBefore(LocalDate.now().plusDays(1)));
            assertTrue(workLog.getDate().isAfter(LocalDate.now().minusDays(8)));
        });
    }

    @Test
    void shouldCreateUsersWithRequiredFields() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        databaseSeeder.run();

        // Assert
        verify(userRepository).saveAll(usersCaptor.capture());
        List<User> seededUsers = usersCaptor.getValue();

        seededUsers.forEach(user -> {
            assertNotNull(user.getUsername());
            assertNotNull(user.getEmail());
            assertNotNull(user.getPassword());
            assertNotNull(user.getFirstName());
            assertNotNull(user.getLastName());
            assertNotNull(user.getRole());
            assertNotNull(user.getDepartment());
            assertNotNull(user.getPosition());
            assertTrue(user.isEnabled());
        });
    }
}
