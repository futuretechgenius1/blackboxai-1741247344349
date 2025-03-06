package com.ems.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WebConfigTest {

    private WebConfig webConfig;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void corsConfigurationAllowsSpecifiedOrigin() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "GET");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("http://localhost:3000", response.getHeader("Access-Control-Allow-Origin"));
        assertTrue(response.getHeader("Access-Control-Allow-Methods").contains("GET"));
    }

    @Test
    void corsConfigurationAllowsCredentials() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://localhost:3000");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
    }

    @Test
    void corsConfigurationAllowsSpecifiedHeaders() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Headers", "Content-Type,Authorization");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        String allowedHeaders = response.getHeader("Access-Control-Allow-Headers");
        assertTrue(allowedHeaders.contains("Content-Type"));
        assertTrue(allowedHeaders.contains("Authorization"));
    }

    @Test
    void corsConfigurationSetsMaxAge() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://localhost:3000");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
    }

    @Test
    void corsConfigurationAllowsAllSpecifiedMethods() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://localhost:3000");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        String allowedMethods = response.getHeader("Access-Control-Allow-Methods");
        List<String> expectedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        expectedMethods.forEach(method -> 
            assertTrue(allowedMethods.contains(method), "Should allow " + method));
    }

    @Test
    void corsConfigurationHandlesActualRequest() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("GET");
        request.addHeader("Origin", "http://localhost:3000");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        assertEquals("http://localhost:3000", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    void corsConfigurationRejectsUnallowedOrigin() throws ServletException, IOException {
        // Arrange
        CorsFilter corsFilter = webConfig.corsFilter();
        request.setMethod("OPTIONS");
        request.addHeader("Origin", "http://malicious-site.com");

        // Act
        corsFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    void webMvcConfigurerImplementsCorsMapping() {
        // Arrange
        TestCorsRegistry registry = new TestCorsRegistry();

        // Act
        webConfig.addCorsMappings(registry);

        // Assert
        assertTrue(registry.hasMappingForPattern("/api/**"));
    }

    // Helper class to test CorsRegistry configuration
    private static class TestCorsRegistry extends CorsRegistry {
        private String pattern;

        @Override
        public CorsRegistry addMapping(String pattern) {
            this.pattern = pattern;
            return super.addMapping(pattern);
        }

        public boolean hasMappingForPattern(String pattern) {
            return this.pattern != null && this.pattern.equals(pattern);
        }
    }
}
