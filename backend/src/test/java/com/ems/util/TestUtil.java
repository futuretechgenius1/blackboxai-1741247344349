package com.ems.util;

import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.RegisterRequest;
import com.ems.dto.worklog.WorkLogRequest;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestUtil {

    public static class UserBuilder {
        private User user;

        private UserBuilder() {
            user = new User();
            user.setId(1L);
            user.setUsername("test.user");
            user.setEmail("test.user@ems.com");
            user.setPassword("Test@123");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(Role.ROLE_EMPLOYEE);
            user.setDepartment("IT");
            user.setPosition("Developer");
            user.setHourlyRate(25.0);
            user.setEnabled(true);
        }

        public static UserBuilder aUser() {
            return new UserBuilder();
        }

        public UserBuilder withId(Long id) {
            user.setId(id);
            return this;
        }

        public UserBuilder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public UserBuilder withRole(Role role) {
            user.setRole(role);
            return this;
        }

        public UserBuilder asAdmin() {
            user.setRole(Role.ROLE_ADMIN);
            return this;
        }

        public User build() {
            return user;
        }
    }

    public static class WorkLogBuilder {
        private WorkLog workLog;

        private WorkLogBuilder() {
            workLog = new WorkLog();
            workLog.setId(1L);
            workLog.setDate(LocalDate.now());
            workLog.setHoursWorked(8.0);
            workLog.setRemarks("Regular work day");
            workLog.setStatus(WorkLog.WorkLogStatus.PENDING);
            workLog.setCreatedAt(LocalDateTime.now());
            workLog.setUpdatedAt(LocalDateTime.now());
        }

        public static WorkLogBuilder aWorkLog() {
            return new WorkLogBuilder();
        }

        public WorkLogBuilder withId(Long id) {
            workLog.setId(id);
            return this;
        }

        public WorkLogBuilder withUser(User user) {
            workLog.setUser(user);
            return this;
        }

        public WorkLogBuilder withStatus(WorkLog.WorkLogStatus status) {
            workLog.setStatus(status);
            return this;
        }

        public WorkLogBuilder withHours(Double hours) {
            workLog.setHoursWorked(hours);
            return this;
        }

        public WorkLog build() {
            return workLog;
        }
    }

    public static class RequestBuilder {
        public static RegisterRequest buildRegisterRequest() {
            String random = UUID.randomUUID().toString().substring(0, 8);
            return RegisterRequest.builder()
                    .username("test.user." + random)
                    .email("test.user." + random + "@ems.com")
                    .password("Test@123")
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.ROLE_EMPLOYEE)
                    .department("IT")
                    .position("Developer")
                    .hourlyRate(25.0)
                    .build();
        }

        public static AuthRequest buildAuthRequest(String username) {
            return AuthRequest.builder()
                    .username(username)
                    .password("Test@123")
                    .build();
        }

        public static WorkLogRequest buildWorkLogRequest() {
            return WorkLogRequest.builder()
                    .date(LocalDate.now())
                    .hoursWorked(8.0)
                    .remarks("Test work log")
                    .build();
        }
    }

    public static void setAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public static MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype()
    );

    public static ResultActions register(MockMvc mockMvc, RegisterRequest request) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType(APPLICATION_JSON_UTF8)
                .content(JsonUtil.toJson(request)));
    }

    public static ResultActions login(MockMvc mockMvc, AuthRequest request) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON_UTF8)
                .content(JsonUtil.toJson(request)));
    }
}
