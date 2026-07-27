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
        String logEntry = "[" + LocalDateTime.now().format(FORMATTER) + "] " + message;
        FilePersistenceEngine.writeText(LOG_FILE, logEntry);
        System.out.println(logEntry);
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
     * Records booking approval.
     */
    public static void logBookingApproved(String bookingID) {
        log("Booking Approved : " + bookingID);
    }

    /**
     * Records booking rejection.
     */
    public static void logBookingRejected(String bookingID) {
        log("Booking Rejected : " + bookingID);
    }

    /**
     * Records admin actions.
     */
    public static void logAdminAction(String action) {
        log("ADMIN ACTION : " + action);
    }

    /**
     * Records user creation.
     */
    public static void logUserCreated(String username) {
        log("User Created : " + username);
    }

    /**
     * Records user deletion.
     */
    public static void logUserDeleted(String username) {
        log("User Deleted : " + username);
    }

    /**
     * Records resource addition.
     */
    public static void logResourceAdded(String resourceId) {
        log("Resource Added : " + resourceId);
    }

    /**
     * Records resource edit.
     */
    public static void logResourceEdited(String resourceId) {
        log("Resource Edited : " + resourceId);
    }

    /**
     * Records resource deletion.
     */
    public static void logResourceDeleted(String resourceId) {
        log("Resource Deleted : " + resourceId);
    }
}