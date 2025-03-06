package com.ems.config;

import com.ems.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new AuthenticationException("Authentication failed") {};
    }

    @Test
    void commence_SetsCorrectResponseStatus() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void commence_SetsCorrectContentType() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    void commence_WritesCorrectErrorResponse() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        String responseBody = ((MockHttpServletResponse) response).getContentAsString();
        Map<String, Object> errorResponse = JsonUtil.fromJson(responseBody, Map.class);

        assertNotNull(errorResponse.get("timestamp"));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, errorResponse.get("status"));
        assertEquals("Unauthorized", errorResponse.get("error"));
        assertEquals("Authentication failed", errorResponse.get("message"));
    }

    @Test
    void commence_HandlesIOException() throws IOException {
        // Arrange
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("Test IO Exception"))
                .when(mockResponse).getWriter();

        // Act & Assert
        assertDoesNotThrow(() -> entryPoint.commence(request, mockResponse, authException));
    }

    @Test
    void commence_SetsCorrectHeaders() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        assertTrue(response.containsHeader("Cache-Control"));
        assertEquals("no-cache, no-store, max-age=0, must-revalidate", 
                response.getHeader("Cache-Control"));
        assertTrue(response.containsHeader("Pragma"));
        assertEquals("no-cache", response.getHeader("Pragma"));
    }

    @Test
    void commence_WithNullAuthenticationException() throws IOException {
        // Act
        entryPoint.commence(request, response, null);

        // Assert
        String responseBody = ((MockHttpServletResponse) response).getContentAsString();
        Map<String, Object> errorResponse = JsonUtil.fromJson(responseBody, Map.class);

        assertEquals("Unauthorized access", errorResponse.get("message"));
    }

    @Test
    void commence_WithCustomMessage() throws IOException {
        // Arrange
        AuthenticationException customException = 
            new AuthenticationException("Custom error message") {};

        // Act
        entryPoint.commence(request, response, customException);

        // Assert
        String responseBody = ((MockHttpServletResponse) response).getContentAsString();
        Map<String, Object> errorResponse = JsonUtil.fromJson(responseBody, Map.class);

        assertEquals("Custom error message", errorResponse.get("message"));
    }

    @Test
    void commence_ResponseContainsRequiredFields() throws IOException {
        // Act
        entryPoint.commence(request, response, authException);

        // Assert
        String responseBody = ((MockHttpServletResponse) response).getContentAsString();
        Map<String, Object> errorResponse = JsonUtil.fromJson(responseBody, Map.class);

        assertNotNull(errorResponse.get("timestamp"));
        assertNotNull(errorResponse.get("status"));
        assertNotNull(errorResponse.get("error"));
        assertNotNull(errorResponse.get("message"));
        assertNotNull(errorResponse.get("path"));
    }

    @Test
    void commence_IncludesRequestPath() throws IOException {
        // Arrange
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/test/path");

        // Act
        entryPoint.commence(mockRequest, response, authException);

        // Assert
        String responseBody = ((MockHttpServletResponse) response).getContentAsString();
        Map<String, Object> errorResponse = JsonUtil.fromJson(responseBody, Map.class);

        assertEquals("/test/path", errorResponse.get("path"));
    }
}
