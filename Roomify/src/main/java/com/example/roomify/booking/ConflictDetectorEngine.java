package com.example.roomify.booking;

import com.example.roomify.exception.ResourceUnavailableException;

import java.time.LocalDateTime;
import java.util.List;
import com.example.roomify.model.Booking;

/**
 * Detects booking conflicts for campus resources.
 */
public class ConflictDetectorEngine {

    /**
     * Checks whether a resource is already booked
     * during the requested time slot.
     */
    public static void checkConflict(
            String resourceID,
            LocalDateTime newStart,
            LocalDateTime newEnd,
            List<Booking> bookings)
            throws ResourceUnavailableException {

        for (Booking booking : bookings) {

            // Skip cancelled bookings
            if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                continue;
            }

            // Only compare bookings for the same resource
            if (!booking.getResourceId().equals(resourceID)) {
                continue;
            }

            /*
             * Two bookings overlap if:
             * New Start < Existing End
             * AND
             * New End > Existing Start
             */

            boolean overlap =
                    newStart.isBefore(booking.getEndTime()) &&
                            newEnd.isAfter(booking.getStartTime());

            if (overlap) {
                throw new ResourceUnavailableException(
                        "Resource is already booked during this time slot."
                );
            }
        }
    }
}