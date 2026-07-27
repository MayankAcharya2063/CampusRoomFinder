package com.example.roomify.persistence;

import com.example.roomify.model.Booking;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading booking records.
 */
public class BookingFileHandler {

    private static final String BOOKING_FILE = "bookings.dat";
    private static final String BOOKING_TEXT_FILE = "bookings.txt";

    /**
     * Saves all booking records.
     */
    public static void saveBookings(List<Booking> bookings) {
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        FilePersistenceEngine.saveObjects(bookings, BOOKING_FILE);
        saveBookingsToText(bookings);
        System.out.println("Saved " + bookings.size() + " bookings to file.");
    }

    /**
     * Loads all booking records.
     */
    @SuppressWarnings("unchecked")
    public static List<Booking> loadBookings() {
        List<Booking> bookings = FilePersistenceEngine.loadObjects(BOOKING_FILE);
        if (bookings == null) {
            bookings = new ArrayList<>();
        }
        System.out.println("Loaded " + bookings.size() + " bookings from file.");
        return bookings;
    }

    /**
     * Save bookings to text file for readability.
     */
    public static void saveBookingsToText(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            FilePersistenceEngine.writeText(BOOKING_TEXT_FILE, "No bookings found.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Booking booking : bookings) {
            sb.append(booking.getBookingId()).append("|")
                    .append(booking.getResourceId()).append("|")
                    .append(booking.getResourceName()).append("|")
                    .append(booking.getRequesterName()).append("|")
                    .append(booking.getStartTime()).append("|")
                    .append(booking.getEndTime()).append("|")
                    .append(booking.getPurpose()).append("|")
                    .append(booking.getStatus())
                    .append("\n");
        }
        FilePersistenceEngine.writeText(BOOKING_TEXT_FILE, sb.toString());
    }
}