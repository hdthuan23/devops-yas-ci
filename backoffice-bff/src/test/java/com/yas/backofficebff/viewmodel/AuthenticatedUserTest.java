package com.yas.backofficebff.viewmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticatedUserTest {

    @Test
    void testAuthenticatedUser_WithUsername_ShouldCreateSuccessfully() {
        // Arrange
        String expectedUsername = "testuser";

        // Act
        AuthenticatedUser user = new AuthenticatedUser(expectedUsername);

        // Assert
        assertNotNull(user);
        assertEquals(expectedUsername, user.username());
    }

    @Test
    void testAuthenticatedUser_WithNullUsername_ShouldCreateSuccessfully() {
        // Arrange & Act
        AuthenticatedUser user = new AuthenticatedUser(null);

        // Assert
        assertNotNull(user);
        assertNull(user.username());
    }

    @Test
    void testAuthenticatedUser_Equality_ShouldWorkCorrectly() {
        // Arrange
        AuthenticatedUser user1 = new AuthenticatedUser("testuser");
        AuthenticatedUser user2 = new AuthenticatedUser("testuser");
        AuthenticatedUser user3 = new AuthenticatedUser("differentuser");

        // Assert
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testAuthenticatedUser_ToString_ShouldContainUsername() {
        // Arrange
        String username = "testuser";
        AuthenticatedUser user = new AuthenticatedUser(username);

        // Act
        String result = user.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(username));
    }
}
