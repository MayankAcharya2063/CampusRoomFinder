package com.example.roomify.validation;

import java.util.regex.Pattern;

/**
 * Global reusable validation framework for UI forms.
 * Provides comprehensive validation methods for all user inputs.
 */
public final class InputValidator {

    // Regex pattern for valid Roomify email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@roomify\\.com$");

    // Password: at least 6 chars, one letter and one number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    // Username pattern: alphanumeric and underscore, 3-20 chars
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // Resource ID pattern: RES- followed by 3-4 digits
    private static final Pattern RESOURCE_ID_PATTERN =
            Pattern.compile("^RES-\\d{3,4}$");

    // Phone number pattern: 10-15 digits with optional + prefix
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    InputValidator() {
        // Prevent object creation
    }

    /**
     * Validate email format.
     *
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password strength.
     *
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Check for empty input.
     *
     * @param text The text to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Validate booking duration in hours.
     *
     * @param hours The duration in hours
     * @return true if between 1 and 4 hours, false otherwise
     */
    public static boolean isValidDuration(int hours) {
        return hours > 0 && hours <= 4;
    }

    /**
     * Validate positive capacity.
     *
     * @param capacity The capacity to validate
     * @return true if > 0, false otherwise
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0;
    }

    /**
     * Validate username format.
     *
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validate resource ID format.
     *
     * @param resourceId The resource ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidResourceId(String resourceId) {
        return resourceId != null && RESOURCE_ID_PATTERN.matcher(resourceId.trim()).matches();
    }

    /**
     * Validate phone number format.
     *
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate that a string is within length limits.
     *
     * @param text The text to validate
     * @param minLength Minimum allowed length
     * @param maxLength Maximum allowed length
     * @return true if within limits, false otherwise
     */
    public static boolean isValidLength(String text, int minLength, int maxLength) {
        if (text == null) return false;
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate that a date is not in the past.
     *
     * @param dateTime The date/time to validate
     * @return true if in the future or present, false if in the past
     */
    public static boolean isNotPastDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null && !dateTime.isBefore(java.time.LocalDateTime.now());
    }

    /**
     * Validate that a time range is valid (start before end).
     *
     * @param start The start time
     * @param end The end time
     * @return true if start is before end, false otherwise
     */
    public static boolean isValidTimeRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return start != null && end != null && start.isBefore(end);
    }

    /**
     * Validate that a string contains only letters and spaces.
     *
     * @param text The text to validate
     * @return true if only letters and spaces, false otherwise
     */
    public static boolean isOnlyLettersAndSpaces(String text) {
        if (text == null) return false;
        return text.matches("^[a-zA-Z\\s]+$");
    }

    /**
     * Validate that a string contains only alphanumeric characters.
     *
     * @param text The text to validate
     * @return true if alphanumeric, false otherwise
     */
    public static boolean isAlphanumeric(String text) {
        if (text == null) return false;
        return text.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Gets a descriptive error message for a failed validation.
     *
     * @param type The validation type that failed
     * @param value The value that was validated
     * @return A descriptive error message
     */
    public static String getValidationErrorMessage(String type, String value) {
        switch (type.toUpperCase()) {
            case "EMAIL":
                return "Please enter a valid Roomify email (e.g., example@roomify.com).";
            case "PASSWORD":
                return "Password must contain at least 6 characters, including one letter and one number.";
            case "USERNAME":
                return "Username must be 3-20 characters and contain only letters, numbers, and underscores.";
            case "RESOURCE_ID":
                return "Resource ID must be in format RES-XXX (e.g., RES-001).";
            case "PHONE":
                return "Please enter a valid phone number (10-15 digits).";
            case "CAPACITY":
                return "Capacity must be a positive number.";
            case "DURATION":
                return "Booking duration must be between 1 and 4 hours.";
            default:
                return "Invalid input: " + value;
        }
    }
}