package com.ems;

import com.ems.config.TestConfiguration;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.model.WorkLog;
import com.ems.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfiguration.class)
@Transactional
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestConfiguration testConfig;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected User adminUser;
    protected User employeeUser;
    protected WorkLog testWorkLog;
    protected DateTimeFormatter dateFormatter;

    @BeforeEach
    void setUp() {
        adminUser = testConfig.testAdminUser();
        employeeUser = testConfig.testEmployeeUser();
        testWorkLog = testConfig.testWorkLog();
        dateFormatter = testConfig.testDateFormatter();
    }

    // Helper methods for HTTP requests
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    protected ResultActions performGetWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token));
    }

    protected ResultActions performPost(String url, Object content) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(content)));
    }

    protected ResultActions performPostWithAuth(String url, Object content, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(content)));
    }

    protected ResultActions performPut(String url, Object content) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(content)));
    }

    protected ResultActions performPutWithAuth(String url, Object content, String token) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(content)));
    }

    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url));
    }

    protected ResultActions performDeleteWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token));
    }

    // Helper methods for creating test data
    protected User createTestUser(String username, String email, Role role) {
        return testConfig.createTestUser(username, email, role);
    }

    protected WorkLog createTestWorkLog(User user, LocalDate date, Double hours, WorkLog.WorkLogStatus status) {
        return testConfig.createTestWorkLog(user, date, hours, status);
    }

    // Helper methods for authentication
    protected String getTestToken(User user) {
        return testConfig.getTestJwtToken(user);
    }

    protected String getAuthorizationHeader(User user) {
        return testConfig.getTestAuthorizationHeader(user);
    }

    // Helper methods for assertions
    protected void assertUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    protected void assertForbidden(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden());
    }

    protected void assertOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    protected void assertCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    protected void assertBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    protected void assertNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    // Helper methods for date handling
    protected String formatDate(LocalDate date) {
        return date.format(dateFormatter);
    }

    protected LocalDate parseDate(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    // Helper methods for cleanup
    protected void clearDatabase() {
        // Implement if needed for specific test cases
    }

    // Helper methods for validation
    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    protected boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9._-]{3,}$");
    }

    protected boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
}
