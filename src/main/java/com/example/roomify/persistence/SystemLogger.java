package com.example.roomify.persistence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Records important system events into a log file.
 */
public class SystemLogger {

    private static final String LOG_FILE = "system_log.txt";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Writes a log message with a timestamp.
     */
    public static void log(String message) {

        String logEntry =
                "[" + LocalDateTime.now().format(FORMATTER) + "] " + message;

        FilePersistenceEngine.writeText(LOG_FILE, logEntry);

    }

    /**
     * Records login activity.
     */
    public static void logLogin(String username) {

        log("User Login : " + username);

    }

    /**
     * Records logout activity.
     */
    public static void logLogout(String username) {

        log("User Logout : " + username);

    }

    /**
     * Records booking creation.
     */
    public static void logBookingCreated(String bookingID) {

        log("Booking Created : " + bookingID);

    }

    /**
     * Records booking cancellation.
     */
    public static void logBookingCancelled(String bookingID) {

        log("Booking Cancelled : " + bookingID);

    }

    /**
     * Records admin actions.
     */
    public static void logAdminAction(String action) {

        log("ADMIN ACTION : " + action);

    }

}