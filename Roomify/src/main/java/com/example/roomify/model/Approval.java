package com.example.roomify.model;

import java.time.LocalDateTime;

public class Approval {

    private String approvalId;
    private String bookingId;          // which booking this approval is for
    private String resourceId;         // which resource that booking is for
    private String requestedByUserId;  // who asked for the booking
    private ApprovalStatus status;
    private String reviewedByAdminId;  // which admin reviewed it (stays null until reviewed)
    private LocalDateTime requestDate;
    private LocalDateTime reviewDate;
    private String comments;           // e.g. reason for rejection or override

    public Approval(String approvalId, String bookingId, String resourceId, String requestedByUserId) {
        this.approvalId = approvalId;
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.requestedByUserId = requestedByUserId;
        this.status = ApprovalStatus.PENDING;
        this.requestDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getApprovalId() { return approvalId; }
    public void setApprovalId(String approvalId) { this.approvalId = approvalId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(String requestedByUserId) { this.requestedByUserId = requestedByUserId; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public String getReviewedByAdminId() { return reviewedByAdminId; }
    public void setReviewedByAdminId(String reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    @Override
    public String toString() {
        return "Approval " + approvalId + " [booking=" + bookingId + ", status=" + status + "]";
    }
}
