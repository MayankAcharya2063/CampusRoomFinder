package com.example.roomify.booking;

import com.example.roomify.exception.InvalidBookingDurationException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Validates booking details before a booking is created.
 */
public class BookingValidator {

    // Maximum booking duration (hours)
    private static final int MAX_DURATION_HOURS = 4;

    /**
     * Validates booking duration and time.
     */
    public static void validateBooking(LocalDateTime startTime,
                                       LocalDateTime endTime)
            throws InvalidBookingDurationException {

        // Booking start time must not be in the past
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingDurationException(
                    "Booking start time cannot be in the past."
            );
        }

        // End time must be after start time
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new InvalidBookingDurationException(
                    "End time must be after the start time."
            );
        }

        // Calculate booking duration
        long hours = Duration.between(startTime, endTime).toHours();

        if (hours > MAX_DURATION_HOURS) {
            throw new InvalidBookingDurationException(
                    "Maximum booking duration is "
                            + MAX_DURATION_HOURS + " hours."
            );
        }
    }

    /**
     * Checks whether the booking starts in the future.
     */
    public static boolean isFutureBooking(LocalDateTime startTime) {
        return startTime.isAfter(LocalDateTime.now());
    }

}