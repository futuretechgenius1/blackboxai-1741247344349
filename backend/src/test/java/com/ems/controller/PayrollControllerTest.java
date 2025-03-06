package com.ems.controller;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.service.PayrollService;
import com.ems.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayrollController.class)
public class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollService payrollService;

    private User testEmployee;
    private Map<String, Object> payrollData;
    private YearMonth currentMonth;

    @BeforeEach
    void setUp() {
        testEmployee = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("test.employee")
                .build();

        currentMonth = YearMonth.now();

        // Setup sample payroll data
        payrollData = new HashMap<>();
        payrollData.put("employeeId", testEmployee.getId());
        payrollData.put("employeeName", testEmployee.getFullName());
        payrollData.put("period", currentMonth.toString());
        payrollData.put("totalHours", 160.0);
        payrollData.put("regularHours", 160.0);
        payrollData.put("overtimeHours", 0.0);

        Map<String, Double> earnings = new HashMap<>();
        earnings.put("regularPay", 4000.0);
        earnings.put("overtimePay", 0.0);
        earnings.put("grossPay", 4000.0);
        payrollData.put("earnings", earnings);

        Map<String, Double> deductions = new HashMap<>();
        deductions.put("tax", 800.0);
        deductions.put("insurance", 200.0);
        deductions.put("pension", 200.0);
        deductions.put("total", 1200.0);
        payrollData.put("deductions", deductions);

        payrollData.put("netPay", 2800.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void calculatePayrollAsAdmin() throws Exception {
        // Arrange
        when(payrollService.calculatePayroll(eq(1L), any(YearMonth.class)))
                .thenReturn(payrollData);

        // Act & Assert
        mockMvc.perform(get("/payroll/calculate/{userId}", 1L)
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.totalHours").value(160.0))
                .andExpect(jsonPath("$.earnings.regularPay").value(4000.0))
                .andExpect(jsonPath("$.netPay").value(2800.0));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void calculatePayrollAsEmployee() throws Exception {
        // Arrange
        when(payrollService.calculatePayroll(eq(1L), any(YearMonth.class)))
                .thenReturn(payrollData);

        // Act & Assert
        mockMvc.perform(get("/payroll/calculate/{userId}", 1L)
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generatePayrollReport() throws Exception {
        // Arrange
        List<Map<String, Object>> reportData = Arrays.asList(payrollData);
        when(payrollService.generatePayrollReport(any(YearMonth.class)))
                .thenReturn(reportData);

        // Act & Assert
        mockMvc.perform(get("/payroll/report")
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$[0].netPay").value(2800.0));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void generatePayrollReportUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/report")
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPayrollSummary() throws Exception {
        // Arrange
        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("totalPayroll", 50000.0);
        summaryData.put("totalEmployees", 5);
        summaryData.put("averageMonthlyPayroll", 10000.0);

        when(payrollService.getPayrollSummary(any(YearMonth.class), any(YearMonth.class)))
                .thenReturn(summaryData);

        // Act & Assert
        mockMvc.perform(get("/payroll/summary")
                .param("startMonth", currentMonth.toString())
                .param("endMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayroll").value(50000.0))
                .andExpect(jsonPath("$.totalEmployees").value(5));
    }

    @Test
    @WithMockUser
    void getMyPayroll() throws Exception {
        // Arrange
        when(payrollService.calculatePayroll(any(), any(YearMonth.class)))
                .thenReturn(payrollData);

        // Act & Assert
        mockMvc.perform(get("/payroll/my-payroll")
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()));
    }

    @Test
    void accessEndpointWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/calculate/{userId}", 1L)
                .param("yearMonth", currentMonth.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void calculatePayrollWithInvalidDate() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/calculate/{userId}", 1L)
                .param("yearMonth", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadPayrollReport() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/payroll/download-report")
                .param("yearMonth", currentMonth.toString())
                .param("format", "pdf"))
                .andExpect(status().isNotImplemented());
    }
}
