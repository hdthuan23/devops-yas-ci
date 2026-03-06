package com.yas.backofficebff.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    /**
     * Validates if username is valid
     * @param username the username to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        
        // Must be at least 3 characters
        if (username.length() < 3) {
            return false;
        }
        
        // Must be alphanumeric with optional dots and underscores
        return username.matches("^[a-zA-Z0-9._]+$");
    }

    /**
     * Extracts first name from full username
     * @param username the username (e.g., "john.doe")
     * @return first part before dot, or full username if no dot
     */
    public String extractFirstName(String username) {
        if (!StringUtils.hasText(username)) {
            return "";
        }
        
        if (username.contains(".")) {
            String[] parts = username.split("\\.");
            return parts[0];
        }
        
        return username;
    }

    /**
     * Formats username for display
     * @param username the username
     * @return formatted username
     */
    public String formatUsernameForDisplay(String username) {
        if (!StringUtils.hasText(username)) {
            return "Anonymous";
        }
        
        // Capitalize first letter
        String firstName = extractFirstName(username);
        if (firstName.length() > 0) {
            return firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
        }
        
        return username;
    }
}
