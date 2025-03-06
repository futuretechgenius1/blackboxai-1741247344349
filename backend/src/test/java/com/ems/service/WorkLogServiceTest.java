package com.ems.service;

import com.ems.dto.worklog.WorkLogRequest;
import com.ems.dto.worklog.WorkLogResponse;
import com.ems.exception.ResourceNotFoundException;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.ems.repository.WorkLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkLogServiceTest {

    @Mock
    private WorkLogRepository workLogRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private WorkLogService workLogService;

    private User testUser;
    private WorkLog testWorkLog;
    private WorkLogRequest workLogRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .hourlyRate(25.0)
                .build();

        workLogRequest = WorkLogRequest.builder()
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Test work log")
                .build();

        testWorkLog = WorkLog.builder()
                .id(1L)
                .user(testUser)
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Test work log")
                .status(WorkLog.WorkLogStatus.PENDING)
                .build();
    }

    @Test
    void createWorkLogSuccessfully() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(workLogRepository.existsByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(false);
        when(workLogRepository.save(any(WorkLog.class))).thenReturn(testWorkLog);

        // Act
        WorkLogResponse response = workLogService.createWorkLog(workLogRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testWorkLog.getId(), response.getId());
        assertEquals(testWorkLog.getHoursWorked(), response.getHoursWorked());
        assertEquals(testWorkLog.getRemarks(), response.getRemarks());
        verify(workLogRepository).save(any(WorkLog.class));
    }

    @Test
    void createWorkLogDuplicateDate() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(workLogRepository.existsByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> workLogService.createWorkLog(workLogRequest));
        verify(workLogRepository, never()).save(any(WorkLog.class));
    }

    @Test
    void getUserWorkLogsSuccessfully() {
        // Arrange
        List<WorkLog> workLogs = Arrays.asList(testWorkLog);
        Page<WorkLog> workLogPage = new PageImpl<>(workLogs);
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(workLogRepository.findByUser(any(User.class), any(Pageable.class)))
                .thenReturn(workLogPage);

        // Act
        Page<WorkLogResponse> response = workLogService.getUserWorkLogs(Pageable.unpaged());

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testWorkLog.getId(), response.getContent().get(0).getId());
    }

    @Test
    void updateWorkLogSuccessfully() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(workLogRepository.findById(anyLong())).thenReturn(Optional.of(testWorkLog));
        when(workLogRepository.save(any(WorkLog.class))).thenReturn(testWorkLog);

        // Act
        WorkLogResponse response = workLogService.updateWorkLog(1L, workLogRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testWorkLog.getId(), response.getId());
        assertEquals(workLogRequest.getHoursWorked(), response.getHoursWorked());
        assertEquals(workLogRequest.getRemarks(), response.getRemarks());
        verify(workLogRepository).save(any(WorkLog.class));
    }

    @Test
    void updateWorkLogNotFound() {
        // Arrange
        when(workLogRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workLogService.updateWorkLog(1L, workLogRequest));
        verify(workLogRepository, never()).save(any(WorkLog.class));
    }

    @Test
    void updateWorkLogStatusAsAdmin() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.ROLE_ADMIN)
                .build();

        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(workLogRepository.findById(anyLong())).thenReturn(Optional.of(testWorkLog));
        when(workLogRepository.save(any(WorkLog.class))).thenReturn(testWorkLog);

        // Act
        WorkLogResponse response = workLogService.updateWorkLogStatus(1L, WorkLog.WorkLogStatus.APPROVED);

        // Assert
        assertNotNull(response);
        assertEquals(WorkLog.WorkLogStatus.APPROVED, testWorkLog.getStatus());
        verify(workLogRepository).save(testWorkLog);
    }

    @Test
    void updateWorkLogStatusAsEmployee() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> workLogService.updateWorkLogStatus(1L, WorkLog.WorkLogStatus.APPROVED));
        verify(workLogRepository, never()).save(any(WorkLog.class));
    }
}
