package com.ems.controller;

import com.ems.dto.worklog.WorkLogRequest;
import com.ems.dto.worklog.WorkLogResponse;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.ems.service.WorkLogService;
import com.ems.util.JsonUtil;
import com.ems.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkLogController.class)
public class WorkLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkLogService workLogService;

    private User testUser;
    private WorkLogRequest workLogRequest;
    private WorkLogResponse workLogResponse;

    @BeforeEach
    void setUp() {
        testUser = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("test.user")
                .build();

        workLogRequest = WorkLogRequest.builder()
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Regular work day")
                .build();

        workLogResponse = WorkLogResponse.builder()
                .id(1L)
                .userId(testUser.getId())
                .userName(testUser.getFullName())
                .date(LocalDate.now())
                .hoursWorked(8.0)
                .remarks("Regular work day")
                .status(WorkLog.WorkLogStatus.PENDING)
                .calculatedPay(200.0) // 8 hours * $25/hour
                .build();
    }

    @Test
    @WithMockUser
    void createWorkLog() throws Exception {
        // Arrange
        when(workLogService.createWorkLog(any(WorkLogRequest.class)))
                .thenReturn(workLogResponse);

        // Act & Assert
        mockMvc.perform(post("/work-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(workLogRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hoursWorked").value(8.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void getUserWorkLogs() throws Exception {
        // Arrange
        List<WorkLogResponse> workLogs = Arrays.asList(workLogResponse);
        Page<WorkLogResponse> workLogPage = new PageImpl<>(workLogs);
        when(workLogService.getUserWorkLogs(any(Pageable.class)))
                .thenReturn(workLogPage);

        // Act & Assert
        mockMvc.perform(get("/work-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].hoursWorked").value(8.0));
    }

    @Test
    @WithMockUser
    void getUserWorkLogsByDateRange() throws Exception {
        // Arrange
        List<WorkLogResponse> workLogs = Arrays.asList(workLogResponse);
        when(workLogService.getUserWorkLogsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(workLogs);

        // Act & Assert
        mockMvc.perform(get("/work-logs/date-range")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].hoursWorked").value(8.0));
    }

    @Test
    @WithMockUser
    void updateWorkLog() throws Exception {
        // Arrange
        when(workLogService.updateWorkLog(eq(1L), any(WorkLogRequest.class)))
                .thenReturn(workLogResponse);

        // Act & Assert
        mockMvc.perform(put("/work-logs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(workLogRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hoursWorked").value(8.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWorkLogStatus() throws Exception {
        // Arrange
        WorkLogResponse approvedResponse = workLogResponse;
        approvedResponse.setStatus(WorkLog.WorkLogStatus.APPROVED);
        when(workLogService.updateWorkLogStatus(eq(1L), any(WorkLog.WorkLogStatus.class)))
                .thenReturn(approvedResponse);

        // Act & Assert
        mockMvc.perform(put("/work-logs/1/status")
                .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void updateWorkLogStatusUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/work-logs/1/status")
                .param("status", "APPROVED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getMonthlyWorkSummary() throws Exception {
        // Arrange
        when(workLogService.getMonthlyWorkSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/work-logs/monthly-summary")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void accessEndpointWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/work-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createWorkLogWithInvalidData() throws Exception {
        // Arrange
        WorkLogRequest invalidRequest = WorkLogRequest.builder()
                .date(null)
                .hoursWorked(-1.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/work-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
