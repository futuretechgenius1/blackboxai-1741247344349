package com.ems;

import com.ems.config.*;
import com.ems.controller.*;
import com.ems.repository.UserRepository;
import com.ems.repository.WorkLogRepository;
import com.ems.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EmployeeManagementSystemApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void controllersAreLoaded() {
        assertNotNull(context.getBean(AuthController.class));
        assertNotNull(context.getBean(UserController.class));
        assertNotNull(context.getBean(WorkLogController.class));
        assertNotNull(context.getBean(PayrollController.class));
    }

    @Test
    void servicesAreLoaded() {
        assertNotNull(context.getBean(AuthService.class));
        assertNotNull(context.getBean(UserService.class));
        assertNotNull(context.getBean(WorkLogService.class));
        assertNotNull(context.getBean(PayrollService.class));
        assertNotNull(context.getBean(JwtService.class));
    }

    @Test
    void repositoriesAreLoaded() {
        assertNotNull(context.getBean(UserRepository.class));
        assertNotNull(context.getBean(WorkLogRepository.class));
    }

    @Test
    void securityConfigurationIsLoaded() {
        assertNotNull(context.getBean(SecurityConfig.class));
        assertNotNull(context.getBean(JwtAuthenticationFilter.class));
        assertNotNull(context.getBean(AuthenticationManager.class));
        assertNotNull(context.getBean(PasswordEncoder.class));
        assertNotNull(context.getBean(CustomAuthenticationEntryPoint.class));
    }

    @Test
    void webConfigurationIsLoaded() {
        assertNotNull(context.getBean(WebConfig.class));
        assertNotNull(context.getBean(CorsConfigurationSource.class));
    }

    @Test
    void openApiConfigurationIsLoaded() {
        assertNotNull(context.getBean(OpenApiConfig.class));
    }

    @Test
    void databaseSeederIsLoaded() {
        assertNotNull(context.getBean(DatabaseSeeder.class));
    }

    @Test
    void requiredPropertiesAreSet() {
        assertNotNull(context.getEnvironment().getProperty("spring.datasource.url"));
        assertNotNull(context.getEnvironment().getProperty("spring.datasource.username"));
        assertNotNull(context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
        assertNotNull(context.getEnvironment().getProperty("jwt.secret"));
        assertNotNull(context.getEnvironment().getProperty("jwt.expiration"));
    }

    @Test
    void serverPortIsConfigured() {
        assertNotNull(context.getEnvironment().getProperty("server.port"));
    }

    @Test
    void databaseConfigurationIsValid() {
        String url = context.getEnvironment().getProperty("spring.datasource.url");
        assertTrue(url.contains("jdbc:"));
    }

    @Test
    void jwtConfigurationIsValid() {
        String secret = context.getEnvironment().getProperty("jwt.secret");
        String expiration = context.getEnvironment().getProperty("jwt.expiration");
        assertNotNull(secret);
        assertTrue(secret.length() >= 32); // Minimum recommended length for JWT secret
        assertNotNull(expiration);
        assertTrue(Integer.parseInt(expiration) > 0);
    }

    @Test
    void corsConfigurationIsValid() {
        CorsConfigurationSource corsConfig = context.getBean(CorsConfigurationSource.class);
        assertNotNull(corsConfig);
    }

    @Test
    void activeProfileIsTest() {
        assertTrue(context.getEnvironment().acceptsProfiles(org.springframework.core.env.Profiles.of("test")));
    }

    @Test
    void hibernateConfigurationIsValid() {
        String ddlAuto = context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        assertNotNull(ddlAuto);
        assertTrue(ddlAuto.equals("create-drop") || ddlAuto.equals("update") || 
                  ddlAuto.equals("validate") || ddlAuto.equals("create"));
    }
}
