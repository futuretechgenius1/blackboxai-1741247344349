package com.ems.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    void roleEnumValues() {
        // Assert
        assertEquals(2, Role.values().length);
        assertNotNull(Role.valueOf("ROLE_ADMIN"));
        assertNotNull(Role.valueOf("ROLE_EMPLOYEE"));
    }

    @Test
    void roleAdminHasCorrectName() {
        // Assert
        assertEquals("ROLE_ADMIN", Role.ROLE_ADMIN.name());
    }

    @Test
    void roleEmployeeHasCorrectName() {
        // Assert
        assertEquals("ROLE_EMPLOYEE", Role.ROLE_EMPLOYEE.name());
    }

    @Test
    void roleToGrantedAuthority() {
        // Act
        SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority(Role.ROLE_ADMIN.name());
        SimpleGrantedAuthority employeeAuthority = new SimpleGrantedAuthority(Role.ROLE_EMPLOYEE.name());

        // Assert
        assertEquals("ROLE_ADMIN", adminAuthority.getAuthority());
        assertEquals("ROLE_EMPLOYEE", employeeAuthority.getAuthority());
    }

    @Test
    void roleComparison() {
        // Assert
        assertNotEquals(Role.ROLE_ADMIN, Role.ROLE_EMPLOYEE);
    }

    @Test
    void roleFromString() {
        // Act & Assert
        assertEquals(Role.ROLE_ADMIN, Role.valueOf("ROLE_ADMIN"));
        assertEquals(Role.ROLE_EMPLOYEE, Role.valueOf("ROLE_EMPLOYEE"));
    }

    @Test
    void invalidRoleFromString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("INVALID_ROLE"));
    }

    @Test
    void roleOrdinalValues() {
        // Assert
        assertTrue(Role.ROLE_ADMIN.ordinal() != Role.ROLE_EMPLOYEE.ordinal());
    }

    @Test
    void roleToString() {
        // Assert
        assertEquals("ROLE_ADMIN", Role.ROLE_ADMIN.toString());
        assertEquals("ROLE_EMPLOYEE", Role.ROLE_EMPLOYEE.toString());
    }

    @Test
    void roleEquality() {
        // Arrange
        Role role1 = Role.ROLE_ADMIN;
        Role role2 = Role.ROLE_ADMIN;
        Role role3 = Role.ROLE_EMPLOYEE;

        // Assert
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
    }

    @Test
    void roleHashCode() {
        // Arrange
        Role role1 = Role.ROLE_ADMIN;
        Role role2 = Role.ROLE_ADMIN;
        Role role3 = Role.ROLE_EMPLOYEE;

        // Assert
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void roleNullSafety() {
        // Assert
        assertNotEquals(Role.ROLE_ADMIN, null);
        assertNotEquals(null, Role.ROLE_ADMIN);
    }

    @Test
    void roleTypeComparison() {
        // Assert
        assertFalse(Role.ROLE_ADMIN.equals("ROLE_ADMIN"));
        assertFalse(Role.ROLE_EMPLOYEE.equals("ROLE_EMPLOYEE"));
    }
}
