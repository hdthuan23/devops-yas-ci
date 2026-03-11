package com.yas.backofficebff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void testIsValidUsername_WithValidUsername_ShouldReturnTrue() {
        assertTrue(userService.isValidUsername("john.doe"));
        assertTrue(userService.isValidUsername("admin"));
        assertTrue(userService.isValidUsername("user_123"));
        assertTrue(userService.isValidUsername("test.user.name"));
    }

    @Test
    void testIsValidUsername_WithNullOrEmpty_ShouldReturnFalse() {
        assertFalse(userService.isValidUsername(null));
        assertFalse(userService.isValidUsername(""));
        assertFalse(userService.isValidUsername("  "));
    }

    @Test
    void testIsValidUsername_WithTooShort_ShouldReturnFalse() {
        assertFalse(userService.isValidUsername("ab"));
        assertFalse(userService.isValidUsername("x"));
    }

    @Test
    void testIsValidUsername_WithInvalidCharacters_ShouldReturnFalse() {
        assertFalse(userService.isValidUsername("john doe"));
        assertFalse(userService.isValidUsername("admin@test"));
        assertFalse(userService.isValidUsername("user-name"));
        assertFalse(userService.isValidUsername("test!user"));
    }

    @Test
    void testExtractFirstName_WithDot_ShouldReturnFirstPart() {
        assertEquals("john", userService.extractFirstName("john.doe"));
        assertEquals("alice", userService.extractFirstName("alice.smith.jones"));
    }

    @Test
    void testExtractFirstName_WithoutDot_ShouldReturnFullUsername() {
        assertEquals("admin", userService.extractFirstName("admin"));
        assertEquals("testuser", userService.extractFirstName("testuser"));
    }

    @Test
    void testExtractFirstName_WithNullOrEmpty_ShouldReturnEmpty() {
        assertEquals("", userService.extractFirstName(null));
        assertEquals("", userService.extractFirstName(""));
        assertEquals("", userService.extractFirstName("  "));
    }

    @Test
    void testFormatUsernameForDisplay_WithValidUsername_ShouldCapitalize() {
        assertEquals("John", userService.formatUsernameForDisplay("john.doe"));
        assertEquals("Admin", userService.formatUsernameForDisplay("admin"));
        assertEquals("Test_user", userService.formatUsernameForDisplay("test_user"));
    }

    @Test
    void testFormatUsernameForDisplay_WithNullOrEmpty_ShouldReturnAnonymous() {
        assertEquals("Anonymous", userService.formatUsernameForDisplay(null));
        assertEquals("Anonymous", userService.formatUsernameForDisplay(""));
        assertEquals("Anonymous", userService.formatUsernameForDisplay("  "));
    }

    @Test
    void testFormatUsernameForDisplay_WithTrailingDot_ShouldCapitalizeFirstName() {
        assertEquals("John", userService.formatUsernameForDisplay("john."));
    }

    @Test
    void testFormatUsernameForDisplay_WithSingleCharacter_ShouldCapitalize() {
        assertEquals("A", userService.formatUsernameForDisplay("a.bc"));
    }
}
