package com.example.roomify.validation;

import java.util.regex.Pattern;

/**
 * Global reusable validation framework for UI forms.
 */
public final class InputValidator {

    // Regex pattern for valid Roomify email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@roomify\\.com$");

    // Password: at least 6 chars, one letter and one number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    private InputValidator() {
        // Prevent object creation
    }

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return email != null &&
                EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password strength.
     */
    public static boolean isValidPassword(String password) {
        return password != null &&
                PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Check for empty input.
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Validate booking duration in hours.
     */
    public static boolean isValidDuration(int hours) {
        return hours > 0 && hours <= 4;
    }

    /**
     * Validate positive capacity.
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0;
    }
}