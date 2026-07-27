package com.example.roomify.persistence;

import com.example.roomify.model.Booking;

import java.util.List;

/**
 * Generates simple system reports.
 */
public class ReportGenerator {

    /**
     * Displays a booking summary report.
     */
    public static void generateBookingReport(List<Booking> bookings) {

        int total = bookings.size();
        int pending = 0;
        int approved = 0;
        int cancelled = 0;

        for (Booking booking : bookings) {

            String status = booking.getStatus();

            if ("PENDING".equalsIgnoreCase(status)) {
                pending++;
            } else if ("APPROVED".equalsIgnoreCase(status)) {
                approved++;
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelled++;
            }
        }

        System.out.println("\n========== BOOKING REPORT ==========");
        System.out.println("Total Bookings      : " + total);
        System.out.println("Pending Bookings    : " + pending);
        System.out.println("Approved Bookings   : " + approved);
        System.out.println("Cancelled Bookings  : " + cancelled);
        System.out.println("====================================");
    }

    /**
     * Displays the total number of users.
     */
    public static void generateUserReport(List<?> users) {

        System.out.println("\n========== USER REPORT ==========");
        System.out.println("Total Users : " + users.size());
        System.out.println("=================================");
    }

    /**
     * Displays the total number of resources.
     */
    public static void generateResourceReport(List<?> resources) {

        System.out.println("\n======== RESOURCE REPORT ========");
        System.out.println("Total Resources : " + resources.size());
        System.out.println("=================================");
    }

}