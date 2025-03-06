package com.ems.controller;

import com.ems.dto.auth.AuthRequest;
import com.ems.dto.auth.AuthResponse;
import com.ems.dto.auth.RegisterRequest;
import com.ems.model.Role;
import com.ems.service.AuthService;
import com.ems.util.JsonUtil;
import com.ems.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("test.user")
                .email("test.user@ems.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .department("IT")
                .position("Developer")
                .hourlyRate(25.0)
                .build();

        authRequest = AuthRequest.builder()
                .username("test.user")
                .password("Test@123")
                .build();

        authResponse = AuthResponse.builder()
                .token("test-jwt-token")
                .username("test.user")
                .email("test.user@ems.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.ROLE_EMPLOYEE)
                .message("Authentication successful")
                .build();
    }

    @Test
    void registerUser() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("test.user"))
                .andExpect(jsonPath("$.message").value("Authentication successful"));
    }

    @Test
    void registerUserWithInvalidData() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = registerRequest;
        invalidRequest.setUsername("");
        invalidRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticateUser() throws Exception {
        // Arrange
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("test.user"));
    }

    @Test
    void authenticateUserWithInvalidCredentials() throws Exception {
        // Arrange
        AuthRequest invalidRequest = AuthRequest.builder()
                .username("invalid")
                .password("invalid")
                .build();

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void validateTokenWithValidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

    @Test
    void validateTokenWithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateTokenWithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithExistingUsername() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    void registerWithExistingEmail() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email is already registered"));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }
}
