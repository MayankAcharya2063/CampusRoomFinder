package com.example.roomify.persistence;

import com.example.roomify.model.Booking;

import java.util.List;

/**
 * Handles saving and loading booking records.
 */
public class BookingFileHandler {

    private static final String BOOKING_FILE = "bookings.dat";

    /**
     * Saves all booking records.
     */
    public static void saveBookings(List<Booking> bookings) {

        FilePersistenceEngine.saveObjects(bookings, BOOKING_FILE);

    }

    /**
     * Loads all booking records.
     */
    public static List<Booking> loadBookings() {

        return FilePersistenceEngine.loadObjects(BOOKING_FILE);

    }

}