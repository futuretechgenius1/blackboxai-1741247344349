package com.ems.integration;

import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.AuthResponse;
import com.ems.dto.auth.RegisterRequest;
import com.ems.dto.worklog.WorkLogRequest;
import com.ems.model.Role;
import com.ems.model.WorkLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login as admin
        RegisterRequest adminRegister = RegisterRequest.builder()
                .username("admin.test")
                .email("admin.test@ems.com")
                .password("Admin@123")
                .firstName("Admin")
                .lastName("Test")
                .role(Role.ROLE_ADMIN)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRegister)));

        AuthRequest adminLogin = new AuthRequest("admin.test", "Admin@123");
        MvcResult adminResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andReturn();

        AuthResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        adminToken = adminResponse.getToken();

        // Register and login as employee
        RegisterRequest employeeRegister = RegisterRequest.builder()
                .username("employee.test")
                .email("employee.test@ems.com")
                .password("Employee@123")
                .firstName("Employee")
                .lastName("Test")
                .role(Role.ROLE_EMPLOYEE)
                .department("IT")
                .position("Developer")
                .hourlyRate(25.0)
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRegister)));

        AuthRequest employeeLogin = new AuthRequest("employee.test", "Employee@123");
        MvcResult employeeResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeLogin)))
                .andReturn();

        AuthResponse employeeResponse = objectMapper.readValue(
                employeeResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        employeeToken = employeeResponse.getToken();
    }

    @Test
    void testAuthenticationFlow() throws Exception {
        // Test invalid login
        AuthRequest invalidLogin = new AuthRequest("invalid", "Invalid@123");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized());

        // Test valid token
        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
    }

    @Test
    void testWorkLogFlow() throws Exception {
        // Create work log as employee
        WorkLogRequest workLogRequest = WorkLogRequest.builder()
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Regular work day")
                .build();

        MvcResult createResult = mockMvc.perform(post("/work-logs")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workLogRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String workLogId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // View work log as employee
        mockMvc.perform(get("/work-logs")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hoursWorked").value(8.0));

        // Update work log status as admin
        mockMvc.perform(put("/work-logs/" + workLogId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", WorkLog.WorkLogStatus.APPROVED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testPayrollFlow() throws Exception {
        // Create approved work log for payroll calculation
        WorkLogRequest workLogRequest = WorkLogRequest.builder()
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Regular work day")
                .build();

        MvcResult createResult = mockMvc.perform(post("/work-logs")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workLogRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String workLogId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Approve work log as admin
        mockMvc.perform(put("/work-logs/" + workLogId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", WorkLog.WorkLogStatus.APPROVED.name()))
                .andExpect(status().isOk());

        // Calculate payroll as admin
        mockMvc.perform(get("/payroll/calculate/{userId}", workLogId)
                .header("Authorization", "Bearer " + adminToken)
                .param("yearMonth", LocalDate.now().toString().substring(0, 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.earnings.regularPay").isNumber());

        // Generate payroll report as admin
        mockMvc.perform(get("/payroll/report")
                .header("Authorization", "Bearer " + adminToken)
                .param("yearMonth", LocalDate.now().toString().substring(0, 7)))
                .andExpect(status().isOk());

        // Try to access admin endpoints as employee (should fail)
        mockMvc.perform(get("/payroll/report")
                .header("Authorization", "Bearer " + employeeToken)
                .param("yearMonth", LocalDate.now().toString().substring(0, 7)))
                .andExpect(status().isForbidden());
    }
}
