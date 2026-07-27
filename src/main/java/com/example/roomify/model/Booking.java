package com.example.roomify.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TEMPORARY placeholder until the real Booking class exists.
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingId;
    private String resourceId;
    private String resourceName;
    private String requesterName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String status; // "PENDING", "APPROVED", "REJECTED", "CANCELLED"

    public Booking(String bookingId, String resourceId, String resourceName, String requesterName,
                   LocalDateTime startTime, LocalDateTime endTime, String purpose, String status) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.requesterName = requesterName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getRequesterName() { return requesterName; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Display-friendly formatted fields, used by table views
    public String getBookingDate() {
        return startTime != null
                ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "";
    }

    public String getStartTimeDisplay() {
        return startTime != null
                ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                : "";
    }

    public String getEndTimeDisplay() {
        return endTime != null
                ? endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                : "";
    }
}