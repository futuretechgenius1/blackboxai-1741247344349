package com.ems.config;

import com.ems.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void publicEndpointsAreAccessible() throws Exception {
        // Test authentication endpoints
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isOk());

        // Test Swagger/OpenAPI endpoints
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/work-logs"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/payroll/report"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanAccessTheirOwnResources() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/work-logs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/payroll/my-payroll"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotAccessAdminResources() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/payroll/report"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAllResources() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/work-logs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/payroll/report"))
                .andExpect(status().isOk());
    }

    @Test
    void optionsRequestsAreAllowed() throws Exception {
        mockMvc.perform(get("/users")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized()); // Still needs auth, but CORS is allowed
    }

    @Test
    @WithMockUser
    void authenticatedUserCanAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/users/profile")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void csrfProtectionIsDisabled() throws Exception {
        mockMvc.perform(post("/work-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk()); // Should not require CSRF token
    }

    @Test
    void corsConfigurationAllowsSpecifiedOrigins() throws Exception {
        mockMvc.perform(options("/users")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder options(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(url);
    }

    private static org.springframework.test.web.servlet.result.MockMvcResultMatchers.HeaderResultMatchers header() {
        return org.springframework.test.web.servlet.result.MockMvcResultMatchers.header();
    }
}
