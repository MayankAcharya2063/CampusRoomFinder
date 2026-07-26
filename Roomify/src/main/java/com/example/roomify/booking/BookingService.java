package com.example.roomify.booking;

import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Booking;
import com.example.roomify.persistence.BookingFileHandler;
import com.example.roomify.persistence.SystemLogger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all booking operations.
 */
public class BookingService {
    // Stores all bookings in memory
    private final List<Booking> bookings = new ArrayList<>();

    /**
     * Creates a new booking.
     */
    public void createBooking(String bookingID,
                              String userID,
                              String resourceID,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              String creatorName)
            throws InvalidBookingDurationException,
            ResourceUnavailableException {
        // Validate booking time
        BookingValidator.validateBooking(startTime, endTime);

        // Check for double booking
        ConflictDetectorEngine.checkConflict(
                resourceID,
                startTime,
                endTime,
                bookings
        );

        // Create booking
        Booking booking = new Booking(
                bookingID,
                resourceID,
                "Study Room",      // temporary resource name
                creatorName,       // requester name
                startTime,
                endTime,
                "General Booking", // purpose
                "PENDING"          // initial status
        );

        bookings.add(booking);
        BookingFileHandler.saveBookings(bookings);
        SystemLogger.logBookingCreated(bookingID);
        System.out.println("Booking created successfully.");
    }

    /**
     * Cancels a booking.
     */
    public boolean cancelBooking(String bookingID) {
        for (Booking booking : bookings) {            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
            booking.setStatus("CANCELLED");
            BookingFileHandler.saveBookings(bookings);
            SystemLogger.logBookingCancelled(bookingID);
            System.out.println("Booking cancelled.");
            return true;
        }
        }
        return false;
    }

    /**
     * Approves a booking.
     */
    public boolean approveBooking(String bookingID) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
                booking.setStatus("APPROVED");
                BookingFileHandler.saveBookings(bookings);
                System.out.println("Booking approved.");
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all bookings.
     */
    public List<Booking> getBookings() {
        return bookings;
    }
}