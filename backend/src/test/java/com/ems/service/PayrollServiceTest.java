package com.ems.service;

import com.ems.exception.PayrollProcessingException;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.ems.repository.UserRepository;
import com.ems.repository.WorkLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private WorkLogRepository workLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PayrollService payrollService;

    private User employee;
    private User admin;
    private WorkLog workLog;
    private YearMonth yearMonth;

    @BeforeEach
    void setUp() {
        yearMonth = YearMonth.now();
        
        employee = User.builder()
                .id(1L)
                .username("employee")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_EMPLOYEE)
                .department("Engineering")
                .hourlyRate(25.0)
                .build();

        admin = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.ROLE_ADMIN)
                .build();

        workLog = WorkLog.builder()
                .id(1L)
                .user(employee)
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .status(WorkLog.WorkLogStatus.APPROVED)
                .build();
    }

    @Test
    void calculatePayrollForEmployee() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(employee);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(workLogRepository.findByUserAndDateBetweenOrderByDateDesc(
                any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(workLog));

        // Act
        Map<String, Object> payroll = payrollService.calculatePayroll(employee.getId(), yearMonth);

        // Assert
        assertNotNull(payroll);
        assertEquals(employee.getId(), payroll.get("employeeId"));
        assertEquals(employee.getFullName(), payroll.get("employeeName"));
        assertEquals(8.0, payroll.get("totalHours"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> earnings = (Map<String, Double>) payroll.get("earnings");
        assertEquals(200.0, earnings.get("regularPay")); // 8 hours * $25/hour
        verify(workLogRepository).findByUserAndDateBetweenOrderByDateDesc(
                any(User.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void calculatePayrollUnauthorizedAccess() {
        // Arrange
        User otherEmployee = User.builder()
                .id(3L)
                .role(Role.ROLE_EMPLOYEE)
                .build();
        
        when(authService.getCurrentUser()).thenReturn(otherEmployee);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(PayrollProcessingException.class,
                () -> payrollService.calculatePayroll(employee.getId(), yearMonth));
    }

    @Test
    void generatePayrollReportAsAdmin() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findAll()).thenReturn(Arrays.asList(employee));
        when(workLogRepository.findByUserAndDateBetweenOrderByDateDesc(
                any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(workLog));

        // Act
        List<Map<String, Object>> report = payrollService.generatePayrollReport(yearMonth);

        // Assert
        assertNotNull(report);
        assertFalse(report.isEmpty());
        assertEquals(1, report.size());
        assertEquals(employee.getId(), report.get(0).get("employeeId"));
    }

    @Test
    void generatePayrollReportUnauthorized() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(employee);

        // Act & Assert
        assertThrows(PayrollProcessingException.class,
                () -> payrollService.generatePayrollReport(yearMonth));
        verify(userRepository, never()).findAll();
    }

    @Test
    void getPayrollSummaryAsAdmin() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findAll()).thenReturn(Arrays.asList(employee));
        when(workLogRepository.findByUserAndDateBetweenOrderByDateDesc(
                any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(workLog));

        // Act
        Map<String, Object> summary = payrollService.getPayrollSummary(yearMonth, yearMonth);

        // Assert
        assertNotNull(summary);
        assertEquals(1, summary.get("totalEmployees"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> departmentTotals = (Map<String, Double>) summary.get("departmentTotals");
        assertTrue(departmentTotals.containsKey("Engineering"));
    }

    @Test
    void getPayrollSummaryUnauthorized() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(employee);

        // Act & Assert
        assertThrows(PayrollProcessingException.class,
                () -> payrollService.getPayrollSummary(yearMonth, yearMonth));
        verify(userRepository, never()).findAll();
    }

    @Test
    void calculateOvertimePayCorrectly() {
        // Arrange
        WorkLog overtimeLog = WorkLog.builder()
                .user(employee)
                .date(LocalDate.now())
                .hoursWorked(10.0) // 8 regular + 2 overtime
                .status(WorkLog.WorkLogStatus.APPROVED)
                .build();

        when(authService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(workLogRepository.findByUserAndDateBetweenOrderByDateDesc(
                any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(overtimeLog));

        // Act
        Map<String, Object> payroll = payrollService.calculatePayroll(employee.getId(), yearMonth);

        // Assert
        assertNotNull(payroll);
        @SuppressWarnings("unchecked")
        Map<String, Double> earnings = (Map<String, Double>) payroll.get("earnings");
        assertEquals(200.0, earnings.get("regularPay")); // 8 hours * $25
        assertEquals(75.0, earnings.get("overtimePay")); // 2 hours * $25 * 1.5
        assertEquals(275.0, earnings.get("grossPay")); // 200 + 75
    }
}
