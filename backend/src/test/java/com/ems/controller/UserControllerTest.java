package com.ems.controller;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.service.UserService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User adminUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        adminUser = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("admin.test")
                .asAdmin()
                .build();

        employeeUser = TestUtil.UserBuilder.aUser()
                .withId(2L)
                .withUsername("employee.test")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        // Arrange
        Page<User> userPage = new PageImpl<>(Arrays.asList(adminUser, employeeUser));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(adminUser.getId()))
                .andExpect(jsonPath("$.content[1].id").value(employeeUser.getId()));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllUsersUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getUserById() throws Exception {
        // Arrange
        when(userService.getUserById(employeeUser.getId())).thenReturn(employeeUser);

        // Act & Assert
        mockMvc.perform(get("/users/{id}", employeeUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeUser.getId()))
                .andExpect(jsonPath("$.username").value(employeeUser.getUsername()));
    }

    @Test
    @WithMockUser
    void updateUser() throws Exception {
        // Arrange
        User updatedUser = employeeUser;
        updatedUser.setFirstName("Updated");
        when(userService.updateUser(eq(employeeUser.getId()), any(User.class)))
                .thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/users/{id}", employeeUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(employeeUser.getId());

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", employeeUser.getId()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(employeeUser.getId());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteUserUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/users/{id}", employeeUser.getId()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserStatus() throws Exception {
        // Arrange
        Map<String, Boolean> status = new HashMap<>();
        status.put("enabled", false);
        
        User disabledUser = employeeUser;
        disabledUser.setEnabled(false);
        when(userService.updateUserStatus(employeeUser.getId(), false))
                .thenReturn(disabledUser);

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/status", employeeUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser
    void checkUsernameAvailability() throws Exception {
        // Arrange
        when(userService.existsByUsername("test.user")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/users/check-username")
                .param("username", "test.user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    @WithMockUser
    void checkEmailAvailability() throws Exception {
        // Arrange
        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/users/check-email")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    @WithMockUser
    void getCurrentUserProfile() throws Exception {
        // Arrange
        when(userService.getUserById(null)).thenReturn(employeeUser);

        // Act & Assert
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeUser.getId()))
                .andExpect(jsonPath("$.username").value(employeeUser.getUsername()));
    }

    @Test
    void accessEndpointWithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }
}
