package com.ems.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void openApiConfigurationExists() {
        assertNotNull(openAPI);
    }

    @Test
    void infoConfigurationIsCorrect() {
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Employee Work Tracking and Payroll Management System API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertTrue(info.getDescription().contains("REST API documentation"));
    }

    @Test
    void contactInformationIsCorrect() {
        Contact contact = openAPI.getInfo().getContact();
        assertNotNull(contact);
        assertEquals("Admin", contact.getName());
        assertEquals("admin@ems.com", contact.getEmail());
        assertEquals("https://ems.com", contact.getUrl());
    }

    @Test
    void licenseInformationIsCorrect() {
        License license = openAPI.getInfo().getLicense();
        assertNotNull(license);
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", license.getUrl());
    }

    @Test
    void serverConfigurationIsCorrect() {
        assertFalse(openAPI.getServers().isEmpty());
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Development Server", server.getDescription());
    }

    @Test
    void securitySchemeIsConfigured() {
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
        assertEquals("JWT Authentication", securityScheme.getDescription());
    }

    @Test
    void globalSecurityRequirementIsConfigured() {
        assertFalse(openAPI.getSecurity().isEmpty());
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertTrue(securityRequirement.containsKey("bearerAuth"));
    }

    @Test
    void componentsAreConfigured() {
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));
    }

    @Test
    void securitySchemeInIsHeader() {
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals(SecurityScheme.In.HEADER, securityScheme.getIn());
    }

    @Test
    void infoDescriptionContainsRequiredInformation() {
        String description = openAPI.getInfo().getDescription();
        assertNotNull(description);
        assertTrue(description.contains("REST API"));
        assertTrue(description.contains("Employee Work Tracking"));
        assertTrue(description.contains("Payroll Management System"));
    }

    @Test
    void serverUrlIsValid() {
        String serverUrl = openAPI.getServers().get(0).getUrl();
        assertTrue(serverUrl.startsWith("http://") || serverUrl.startsWith("https://"));
        assertFalse(serverUrl.endsWith("/"));
    }

    @Test
    void contactInformationIsValid() {
        Contact contact = openAPI.getInfo().getContact();
        assertTrue(contact.getEmail().contains("@"));
        assertTrue(contact.getUrl().startsWith("http"));
    }
}
