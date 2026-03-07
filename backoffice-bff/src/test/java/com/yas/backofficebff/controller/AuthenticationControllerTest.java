package com.yas.backofficebff.controller;

import com.yas.backofficebff.viewmodel.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        authenticationController = new AuthenticationController();
    }

    @Test
    void testUser_WhenAuthenticated_ShouldReturnAuthenticatedUser() {
        // Arrange
        String expectedUsername = "testuser";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("preferred_username", expectedUsername);
        attributes.put("sub", "1234567890");
        attributes.put("email", "testuser@example.com");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
            attributes,
            "sub"
        );

        // Act
        ResponseEntity<AuthenticatedUser> response = authenticationController.user(mockOAuth2User);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expectedUsername, response.getBody().username());
    }

    @Test
    void testUser_WhenUsernameIsNull_ShouldReturnNullUsername() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "1234567890");
        attributes.put("email", "testuser@example.com");
        // Note: preferred_username is not set

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
            attributes,
            "sub"
        );

        // Act
        ResponseEntity<AuthenticatedUser> response = authenticationController.user(mockOAuth2User);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNull(response.getBody().username());
    }

    @Test
    void testUser_WithDifferentAttributes_ShouldExtractUsername() {
        // Arrange
        String username = "admin";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("preferred_username", username);
        attributes.put("sub", "admin-id");
        attributes.put("email", "admin@example.com");
        attributes.put("name", "Admin User");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
            attributes,
            "sub"
        );

        // Act
        ResponseEntity<AuthenticatedUser> response = authenticationController.user(mockOAuth2User);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().username());
    }
}
