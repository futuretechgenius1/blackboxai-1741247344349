package com.ems.service;

import com.ems.model.Role;
import com.ems.model.User;
import com.ems.util.TestUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        testUser = TestUtil.UserBuilder.aUser()
                .withId(1L)
                .withUsername("test.user")
                .build();
    }

    @Test
    void generateTokenSuccessfully() {
        // Act
        token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateTokenWithExtraClaims() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", Role.ROLE_EMPLOYEE.name());
        extraClaims.put("userId", testUser.getId());

        // Act
        token = jwtService.generateToken(extraClaims, testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify extra claims
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(Role.ROLE_EMPLOYEE.name(), claims.get("role"));
        assertEquals(testUser.getId().intValue(), ((Integer) claims.get("userId")).intValue());
    }

    @Test
    void extractUsernameFromToken() {
        // Arrange
        token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void validateTokenSuccessfully() {
        // Arrange
        token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateTokenWithWrongUser() {
        // Arrange
        token = jwtService.generateToken(testUser);
        UserDetails wrongUser = TestUtil.UserBuilder.aUser()
                .withUsername("wrong.user")
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, wrongUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateExpiredToken() throws InterruptedException {
        // Arrange - Generate token that expires quickly
        token = jwtService.generateToken(new HashMap<>(), testUser);

        // Wait for token to expire (assuming test environment has very short expiration)
        Thread.sleep(1000); // Adjust based on test configuration

        // Act & Assert
        assertFalse(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void extractClaimFromToken() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "testValue");
        token = jwtService.generateToken(extraClaims, testUser);

        // Act
        String customClaim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));

        // Assert
        assertEquals("testValue", customClaim);
    }

    @Test
    void extractAllClaimsFromToken() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", Role.ROLE_EMPLOYEE.name());
        extraClaims.put("userId", testUser.getId());
        token = jwtService.generateToken(extraClaims, testUser);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals(testUser.getUsername(), claims.getSubject());
        assertEquals(Role.ROLE_EMPLOYEE.name(), claims.get("role"));
        assertEquals(testUser.getId().intValue(), ((Integer) claims.get("userId")).intValue());
    }

    @Test
    void tokenContainsRequiredClaims() {
        // Arrange
        token = jwtService.generateToken(testUser);
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(claims.getSubject()); // Username
        assertNotNull(claims.getIssuedAt()); // Issue date
        assertNotNull(claims.getExpiration()); // Expiration date
    }

    @Test
    void generateDifferentTokensForSameUser() {
        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(testUser);

        // Assert
        assertNotEquals(token1, token2);
    }
}
