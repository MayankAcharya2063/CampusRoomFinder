package com.example.roomify.booking;

import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Booking;
import com.example.roomify.persistence.BookingFileHandler;
import com.example.roomify.persistence.SystemLogger;
import com.example.roomify.service.ApprovalWorkflowService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all booking operations.
 */
public class BookingService {

    private static BookingService instance;

    // Stores all bookings in memory, seeded from disk on startup
    private final List<Booking> bookings;

    public BookingService() {
        List<Booking> loaded = BookingFileHandler.loadBookings();
        this.bookings = new ArrayList<>(loaded);
    }

    public static synchronized BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
        }
        return instance;
    }

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

        // Link this booking into the approval workflow so admins can act on it
        ApprovalWorkflowService.getInstance().submitForApproval(bookingID, resourceID, userID);
    }

    /**
     * Cancels a booking.
     */
    public boolean cancelBooking(String bookingID) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
                booking.setStatus("CANCELLED");
                BookingFileHandler.saveBookings(bookings);
                SystemLogger.logBookingCancelled(bookingID);
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
                SystemLogger.logAdminAction("Booking Approved : " + bookingID);
                return true;
            }
        }
        return false;
    }

    /**
     * Rejects a booking.
     */
    public boolean rejectBooking(String bookingID, String reason) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
                booking.setStatus("REJECTED");
                BookingFileHandler.saveBookings(bookings);
                SystemLogger.logAdminAction("Booking Rejected : " + bookingID + ". Reason: " + reason);
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

    public void refreshBookings() {
        // Method intentionally left blank - refresh logic handled elsewhere
    }
}