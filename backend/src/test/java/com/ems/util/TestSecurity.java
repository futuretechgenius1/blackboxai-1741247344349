package com.ems.util;

import com.ems.model.Role;
import com.ems.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for security-related test functionality
 */
public final class TestSecurity {

    private static final String SECRET_KEY = "testSecretKeyWithAtLeast32CharactersForTesting";
    private static final long TOKEN_VALIDITY = 3600000; // 1 hour
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private TestSecurity() {
        // Private constructor to prevent instantiation
    }

    /**
     * JWT token generation methods
     */
    public static String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Authentication context methods
     */
    public static void setAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user,
            null,
            Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Password encoding methods
     */
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Mock user creation methods
     */
    public static User createMockAdmin() {
        return User.builder()
                .id(1L)
                .username("admin.test")
                .password(encodePassword("Admin@123"))
                .email("admin.test@ems.com")
                .firstName("Admin")
                .lastName("Test")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();
    }

    public static User createMockEmployee() {
        return User.builder()
                .id(2L)
                .username("employee.test")
                .password(encodePassword("Employee@123"))
                .email("employee.test@ems.com")
                .firstName("Employee")
                .lastName("Test")
                .role(Role.ROLE_EMPLOYEE)
                .enabled(true)
                .build();
    }

    /**
     * Spring Security test utilities
     */
    public static RequestPostProcessor withUser(User user) {
        return SecurityMockMvcRequestPostProcessors.user(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""));
    }

    public static WithMockUser withMockUser(Role role) {
        return new WithMockUser(
            username = "test.user",
            roles = role.name().replace("ROLE_", "")
        );
    }

    /**
     * Authorization header methods
     */
    public static String createAuthorizationHeader(String token) {
        return "Bearer " + token;
    }

    public static String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * Security validation methods
     */
    public static boolean isValidToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasRole(Authentication auth, Role role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.name()));
    }

    /**
     * Test security context builders
     */
    public static class SecurityContextBuilder {
        private User user;
        private String token;
        private Set<Role> roles = new HashSet<>();

        public SecurityContextBuilder withUser(User user) {
            this.user = user;
            return this;
        }

        public SecurityContextBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        public SecurityContextBuilder withRole(Role role) {
            this.roles.add(role);
            return this;
        }

        public void build() {
            if (user == null) {
                user = createMockEmployee();
            }
            if (token == null) {
                token = generateToken(user);
            }
            if (roles.isEmpty()) {
                roles.add(user.getRole());
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                token,
                roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .toList()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
        }
    }

    /**
     * Security test configuration
     */
    public static class SecurityTestConfig {
        private static final Map<String, Object> config = new HashMap<>();

        public static void setConfigValue(String key, Object value) {
            config.put(key, value);
        }

        public static Object getConfigValue(String key) {
            return config.get(key);
        }

        public static void clearConfig() {
            config.clear();
        }
    }
}
