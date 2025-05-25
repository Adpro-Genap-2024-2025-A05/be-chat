package id.ac.ui.cs.advprog.bechat.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    @DisplayName("contains() should return true for valid role names")
    void testContainsValidRoles() {
        assertTrue(Role.contains("PACILIAN"));
        assertTrue(Role.contains("CAREGIVER"));
    }

    @Test
    @DisplayName("contains() should return false for invalid role names")
    void testContainsInvalidRoles() {
        assertFalse(Role.contains("pacilian")); 
        assertFalse(Role.contains("ADMIN"));
        assertFalse(Role.contains(""));         
        assertFalse(Role.contains(null));       
    }

    @Test
    @DisplayName("getValue() should return correct role string")
    void testGetValue() {
        assertEquals("PACILIAN", Role.PACILIAN.getValue());
        assertEquals("CAREGIVER", Role.CAREGIVER.getValue());
    }
}
