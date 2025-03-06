package com.ems.config;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.service.JwtService;
import com.ems.util.TestUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("test.user")
                .build();
        validToken = "valid.jwt.token";
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalWithValidToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getUsername());
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser.getUsername(), 
            SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternalWithNoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternalWithInvalidAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternalWithInvalidToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getUsername());
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalWithValidTokenAndAdminRole() throws ServletException, IOException {
        // Arrange
        testUser.setRole(Role.ROLE_ADMIN);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(testUser.getUsername());
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void doFilterInternalWithNonExistentUser() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("nonexistent.user");
        when(userDetailsService.loadUserByUsername("nonexistent.user"))
                .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalWithExpiredToken() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Token expired"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipFilterForPublicEndpoints() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }
}
