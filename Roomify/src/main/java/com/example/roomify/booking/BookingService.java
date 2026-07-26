package com.example.roomify.booking;

import com.example.roomify.exception.InvalidBookingDurationException;
import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.model.Booking;
import com.example.roomify.persistence.BookingFileHandler;
import com.example.roomify.persistence.SystemLogger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all booking operations.
 */
public class BookingService {

    // Stores all bookings in memory
    private final List<Booking> bookings = new ArrayList<>();
    private boolean bookingsLoaded = false;

    public BookingService() {
        loadBookingsFromFile();
    }

    /**
     * Load bookings from file.
     */
    private void loadBookingsFromFile() {
        List<Booking> loaded = BookingFileHandler.loadBookings();
        if (loaded != null && !loaded.isEmpty()) {
            bookings.clear();
            // Use a set to prevent duplicates
            java.util.Set<String> uniqueIds = new java.util.HashSet<>();
            for (Booking booking : loaded) {
                if (!uniqueIds.contains(booking.getBookingId())) {
                    uniqueIds.add(booking.getBookingId());
                    bookings.add(booking);
                }
            }
            bookingsLoaded = true;
            System.out.println("Loaded " + bookings.size() + " unique bookings from file.");
        } else {
            bookings.clear();
            System.out.println("No bookings found in file.");
        }
    }

    /**
     * Save bookings to file.
     */
    private void saveBookingsToFile() {
        // Remove duplicates before saving
        java.util.Map<String, Booking> uniqueMap = new java.util.LinkedHashMap<>();
        for (Booking booking : bookings) {
            uniqueMap.put(booking.getBookingId(), booking);
        }
        List<Booking> uniqueList = new ArrayList<>(uniqueMap.values());
        BookingFileHandler.saveBookings(uniqueList);
        System.out.println("Saved " + uniqueList.size() + " unique bookings to file.");
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

        // Check if booking already exists
        for (Booking existing : bookings) {
            if (existing.getBookingId().equals(bookingID)) {
                System.out.println("Booking already exists: " + bookingID);
                return;
            }
        }

        // Create booking
        Booking booking = new Booking(
                bookingID,
                resourceID,
                "Study Room", // temporary resource name
                creatorName,
                startTime,
                endTime,
                "General Booking",
                "PENDING"
        );

        bookings.add(booking);
        saveBookingsToFile();
        SystemLogger.logBookingCreated(bookingID);
        System.out.println("Booking created successfully: " + bookingID);
    }

    /**
     * Cancels a booking.
     */
    public boolean cancelBooking(String bookingID) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
                booking.setStatus("CANCELLED");
                saveBookingsToFile();
                SystemLogger.logBookingCancelled(bookingID);
                System.out.println("Booking cancelled: " + bookingID);
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
                saveBookingsToFile();
                SystemLogger.logBookingApproved(bookingID);
                System.out.println("Booking approved: " + bookingID);
                return true;
            }
        }
        return false;
    }

    /**
     * Rejects a booking.
     */
    public boolean rejectBooking(String bookingID) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingID)) {
                booking.setStatus("REJECTED");
                saveBookingsToFile();
                SystemLogger.logBookingRejected(bookingID);
                System.out.println("Booking rejected: " + bookingID);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all bookings.
     */
    public List<Booking> getBookings() {
        // Reload from file to ensure we have latest data
        loadBookingsFromFile();
        return new ArrayList<>(bookings);
    }

    /**
     * Gets bookings for a specific user.
     */
    public List<Booking> getBookingsForUser(String userName) {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getRequesterName().equals(userName)) {
                userBookings.add(booking);
            }
        }
        return userBookings;
    }

    /**
     * Gets pending bookings.
     */
    public List<Booking> getPendingBookings() {
        List<Booking> pending = new ArrayList<>();
        for (Booking booking : bookings) {
            if ("PENDING".equalsIgnoreCase(booking.getStatus())) {
                pending.add(booking);
            }
        }
        return pending;
    }

    /**
     * Force reload bookings from file.
     */
    public void refreshBookings() {
        loadBookingsFromFile();
    }

    /**
     * Displays all bookings.
     */
    public void displayBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println("----------------------------------------");
            System.out.println(booking);
        }
    }
}