package com.example.roomify.validation;

import com.example.roomify.model.User;
import com.example.roomify.security.PasswordEncoder;
import com.example.roomify.service.ResourceStatusManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FormValidator - Comprehensive form validation utility for UI forms.
 * Handles validation for all user inputs across the application.
 * Works in conjunction with InputValidator for basic validation and
 * AlertFactory for displaying validation errors.
 */
public class FormValidator {

    private static FormValidator instance;
    private final ResourceStatusManager resourceStatusManager;

    // Booking validation constants
    private static final int MAX_BOOKING_DURATION_HOURS = 4;
    private static final int MIN_BOOKING_DURATION_HOURS = 1;
    private static final int MAX_ADVANCE_BOOKING_DAYS = 30;

    private FormValidator() {
        this.resourceStatusManager = ResourceStatusManager.getInstance();
    }

    public static FormValidator getInstance() {
        if (instance == null) {
            instance = new FormValidator();
        }
        return instance;
    }

    // ==================== LOGIN VALIDATION ====================

    /**
     * Validates login form inputs.
     *
     * @param email The email to validate
     * @param password The password to validate
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateLogin(String email, String password) {
        // Check for empty fields
        if (InputValidator.isNullOrEmpty(email)) {
            return ValidationResult.failure("Email is required.");
        }
        if (InputValidator.isNullOrEmpty(password)) {
            return ValidationResult.failure("Password is required.");
        }

        // Validate email format
        if (!InputValidator.isValidEmail(email)) {
            return ValidationResult.failure("Please enter a valid Roomify email (e.g., example@roomify.com).");
        }

        // Validate password strength
        if (!InputValidator.isValidPassword(password)) {
            return ValidationResult.failure("Password must contain at least 6 characters, including one letter and one number.");
        }

        return ValidationResult.success();
    }

    // ==================== BOOKING VALIDATION ====================

    /**
     * Validates booking form inputs.
     *
     * @param resourceId The resource ID to book
     * @param startTime The start time of the booking
     * @param endTime The end time of the booking
     * @param user The user making the booking
     * @param existingBookings List of existing bookings for conflict checking
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateBooking(String resourceId, LocalDateTime startTime,
                                            LocalDateTime endTime, User user,
                                            List<?> existingBookings) {
        // 1. Check for null values
        if (resourceId == null || resourceId.trim().isEmpty()) {
            return ValidationResult.failure("Please select a resource.");
        }
        if (startTime == null) {
            return ValidationResult.failure("Please select a start time.");
        }
        if (endTime == null) {
            return ValidationResult.failure("Please select an end time.");
        }
        if (user == null) {
            return ValidationResult.failure("User not logged in. Please login again.");
        }

        // 2. Validate time range
        if (!startTime.isBefore(endTime)) {
            return ValidationResult.failure("Start time must be before end time.");
        }

        // 3. Validate booking duration
        long durationHours = ChronoUnit.HOURS.between(startTime, endTime);
        if (durationHours < MIN_BOOKING_DURATION_HOURS) {
            return ValidationResult.failure("Booking duration must be at least " +
                    MIN_BOOKING_DURATION_HOURS + " hour(s).");
        }
        if (durationHours > MAX_BOOKING_DURATION_HOURS) {
            return ValidationResult.failure("Booking duration cannot exceed " +
                    MAX_BOOKING_DURATION_HOURS + " hours.");
        }

        // 4. Validate start time is in the future
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isBefore(now)) {
            return ValidationResult.failure("Booking start time must be in the future.");
        }

        // 5. Validate advance booking limit
        LocalDateTime maxAdvance = now.plusDays(MAX_ADVANCE_BOOKING_DAYS);
        if (startTime.isAfter(maxAdvance)) {
            return ValidationResult.failure("Cannot book more than " +
                    MAX_ADVANCE_BOOKING_DAYS + " days in advance.");
        }

        // 6. Validate resource availability
        try {
            com.example.roomify.model.ResourceStatus status =
                    resourceStatusManager.getStatus(resourceId);
            if (status != com.example.roomify.model.ResourceStatus.AVAILABLE) {
                return ValidationResult.failure("Resource is currently " + status +
                        ". Please select another resource.");
            }
        } catch (Exception e) {
            return ValidationResult.failure("Error checking resource availability: " + e.getMessage());
        }

        // 7. Check for double booking (if existing bookings provided)
        if (existingBookings != null && !existingBookings.isEmpty()) {
            for (Object obj : existingBookings) {
                if (obj instanceof com.example.roomify.model.Booking) {
                    com.example.roomify.model.Booking existing =
                            (com.example.roomify.model.Booking) obj;

                    // Skip cancelled bookings - using getStatus() instead of getBookingStatus()
                    if ("CANCELLED".equals(existing.getStatus())) {
                        continue;
                    }

                    // Check if same resource
                    if (!existing.getResourceId().equals(resourceId)) {
                        continue;
                    }

                    // Check for time overlap
                    boolean overlap = !(endTime.isBefore(existing.getStartTime()) ||
                            endTime.equals(existing.getStartTime()) ||
                            startTime.isAfter(existing.getEndTime()) ||
                            startTime.equals(existing.getEndTime()));

                    if (overlap) {
                        return ValidationResult.failure(
                                "Resource is already booked for the selected time slot.\n" +
                                        "Existing booking: " + existing.getStartTime() + " - " +
                                        existing.getEndTime() + " by " + existing.getRequesterName()
                        );
                    }
                }
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validates booking cancellation.
     *
     * @param bookingId The booking ID to cancel
     * @param user The user attempting cancellation
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateBookingCancellation(String bookingId, User user) {
        if (InputValidator.isNullOrEmpty(bookingId)) {
            return ValidationResult.failure("Booking ID is required.");
        }
        if (user == null) {
            return ValidationResult.failure("User not logged in. Please login again.");
        }

        // Check if user is authorized (admin can cancel any booking)
        if (user.getRole() != com.example.roomify.UserRole.ADMIN) {
            // Non-admin users can only cancel their own bookings
            // This check is typically done in the service layer
            return ValidationResult.success();
        }

        return ValidationResult.success();
    }

    // ==================== RESOURCE VALIDATION ====================

    /**
     * Validates resource management form inputs.
     *
     * @param resourceId The resource ID
     * @param resourceName The resource name
     * @param type The resource type
     * @param location The resource location
     * @param capacity The resource capacity
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateResource(String resourceId, String resourceName,
                                             String type, String location, int capacity) {
        // Validate resource ID
        if (InputValidator.isNullOrEmpty(resourceId)) {
            return ValidationResult.failure("Resource ID is required.");
        }
        if (!InputValidator.isValidResourceId(resourceId)) {
            return ValidationResult.failure("Resource ID must be in format RES-XXX (e.g., RES-001).");
        }

        // Validate resource name
        if (InputValidator.isNullOrEmpty(resourceName)) {
            return ValidationResult.failure("Resource name is required.");
        }
        if (!InputValidator.isValidLength(resourceName, 2, 100)) {
            return ValidationResult.failure("Resource name must be between 2 and 100 characters.");
        }

        // Validate type
        if (InputValidator.isNullOrEmpty(type)) {
            return ValidationResult.failure("Resource type is required.");
        }

        // Validate location
        if (InputValidator.isNullOrEmpty(location)) {
            return ValidationResult.failure("Location is required.");
        }

        // Validate capacity
        if (capacity <= 0) {
            return ValidationResult.failure("Capacity must be a positive number.");
        }
        if (capacity > 1000) {
            return ValidationResult.failure("Capacity cannot exceed 1000.");
        }

        return ValidationResult.success();
    }

    /**
     * Validates search/filter inputs.
     *
     * @param searchTerm The search term
     * @param type The resource type filter
     * @param location The location filter
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateSearch(String searchTerm, String type, String location) {
        // All search parameters are optional, so no validation failures
        // But we can add length limits
        if (searchTerm != null && searchTerm.length() > 100) {
            return ValidationResult.failure("Search term is too long (max 100 characters).");
        }
        if (type != null && type.length() > 50) {
            return ValidationResult.failure("Type filter is too long (max 50 characters).");
        }
        if (location != null && location.length() > 50) {
            return ValidationResult.failure("Location filter is too long (max 50 characters).");
        }

        return ValidationResult.success();
    }

    // ==================== USER MANAGEMENT VALIDATION ====================

    /**
     * Validates user registration/management form inputs.
     *
     * @param userId The user ID
     * @param name The user's full name
     * @param email The user's email
     * @param password The user's password
     * @param role The user's role
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateUser(String userId, String name, String email,
                                         String password, String role) {
        // Validate user ID
        if (InputValidator.isNullOrEmpty(userId)) {
            return ValidationResult.failure("User ID is required.");
        }
        if (!InputValidator.isValidLength(userId, 3, 20)) {
            return ValidationResult.failure("User ID must be between 3 and 20 characters.");
        }

        // Validate name
        if (InputValidator.isNullOrEmpty(name)) {
            return ValidationResult.failure("Name is required.");
        }
        if (!InputValidator.isOnlyLettersAndSpaces(name)) {
            return ValidationResult.failure("Name must contain only letters and spaces.");
        }
        if (!InputValidator.isValidLength(name, 2, 50)) {
            return ValidationResult.failure("Name must be between 2 and 50 characters.");
        }

        // Validate email
        if (InputValidator.isNullOrEmpty(email)) {
            return ValidationResult.failure("Email is required.");
        }
        if (!InputValidator.isValidEmail(email)) {
            return ValidationResult.failure("Please enter a valid Roomify email (e.g., example@roomify.com).");
        }

        // Validate password (optional for updates, required for new users)
        if (password != null && !password.isEmpty()) {
            if (!InputValidator.isValidPassword(password)) {
                return ValidationResult.failure("Password must contain at least 6 characters, including one letter and one number.");
            }
        }

        // Validate role
        if (InputValidator.isNullOrEmpty(role)) {
            return ValidationResult.failure("Role is required.");
        }
        try {
            com.example.roomify.UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("Invalid role. Must be STUDENT, STAFF, or ADMIN.");
        }

        return ValidationResult.success();
    }

    // ==================== PROFILE UPDATE VALIDATION ====================

    /**
     * Validates profile update form inputs.
     *
     * @param name The user's full name
     * @param email The user's email
     * @param currentPassword The current password (for verification)
     * @param newPassword The new password (optional)
     * @param user The current user
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateProfileUpdate(String name, String email,
                                                  String currentPassword, String newPassword,
                                                  User user) {
        // Validate name
        if (InputValidator.isNullOrEmpty(name)) {
            return ValidationResult.failure("Name is required.");
        }
        if (!InputValidator.isOnlyLettersAndSpaces(name)) {
            return ValidationResult.failure("Name must contain only letters and spaces.");
        }
        if (!InputValidator.isValidLength(name, 2, 50)) {
            return ValidationResult.failure("Name must be between 2 and 50 characters.");
        }

        // Validate email
        if (InputValidator.isNullOrEmpty(email)) {
            return ValidationResult.failure("Email is required.");
        }
        if (!InputValidator.isValidEmail(email)) {
            return ValidationResult.failure("Please enter a valid Roomify email (e.g., example@roomify.com).");
        }

        // If password change is requested
        if (newPassword != null && !newPassword.isEmpty()) {
            // Current password is required
            if (InputValidator.isNullOrEmpty(currentPassword)) {
                return ValidationResult.failure("Current password is required to change password.");
            }

            // Verify current password
            if (user != null && !PasswordEncoder.matches(currentPassword, user.getPassword())) {
                return ValidationResult.failure("Current password is incorrect.");
            }

            // Validate new password
            if (!InputValidator.isValidPassword(newPassword)) {
                return ValidationResult.failure("New password must contain at least 6 characters, including one letter and one number.");
            }

            // New password must be different from current
            if (user != null && PasswordEncoder.matches(newPassword, user.getPassword())) {
                return ValidationResult.failure("New password must be different from current password.");
            }
        }

        return ValidationResult.success();
    }

    // ==================== DATE/TIME VALIDATION ====================

    /**
     * Validates date/time input.
     *
     * @param dateTime The date/time to validate
     * @param allowPast Whether to allow past dates
     * @param maxDaysInFuture Maximum days in the future allowed
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateDateTime(LocalDateTime dateTime, boolean allowPast,
                                             int maxDaysInFuture) {
        if (dateTime == null) {
            return ValidationResult.failure("Date and time is required.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Check if in the past
        if (!allowPast && dateTime.isBefore(now)) {
            return ValidationResult.failure("Date and time cannot be in the past.");
        }

        // Check if too far in the future
        if (maxDaysInFuture > 0) {
            LocalDateTime maxFuture = now.plusDays(maxDaysInFuture);
            if (dateTime.isAfter(maxFuture)) {
                return ValidationResult.failure("Cannot select a date more than " +
                        maxDaysInFuture + " days in the future.");
            }
        }

        return ValidationResult.success();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Validates that a string matches a specific pattern.
     *
     * @param value The value to validate
     * @param pattern The regex pattern to match
     * @param fieldName The name of the field (for error message)
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validatePattern(String value, String pattern, String fieldName) {
        if (InputValidator.isNullOrEmpty(value)) {
            return ValidationResult.failure(fieldName + " is required.");
        }
        if (!value.matches(pattern)) {
            return ValidationResult.failure(fieldName + " format is invalid.");
        }
        return ValidationResult.success();
    }

    /**
     * Validates a numeric range.
     *
     * @param value The numeric value to validate
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @param fieldName The name of the field (for error message)
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateRange(double value, double min, double max, String fieldName) {
        if (value < min) {
            return ValidationResult.failure(fieldName + " must be at least " + min + ".");
        }
        if (value > max) {
            return ValidationResult.failure(fieldName + " must be at most " + max + ".");
        }
        return ValidationResult.success();
    }

    /**
     * Validates a field is not empty.
     *
     * @param value The value to check
     * @param fieldName The name of the field (for error message)
     * @return ValidationResult containing success/failure and error message
     */
    public ValidationResult validateRequired(String value, String fieldName) {
        if (InputValidator.isNullOrEmpty(value)) {
            return ValidationResult.failure(fieldName + " is required.");
        }
        return ValidationResult.success();
    }

    // ==================== INNER CLASS ====================

    /**
     * ValidationResult - Represents the result of a validation operation.
     * Contains success/failure status and an optional error message.
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailure() {
            return !success;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}