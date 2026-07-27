package com.example.roomify.service;

import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.exception.UnauthorizedAccessException;
import com.example.roomify.model.Admin;
import com.example.roomify.model.Approval;
import com.example.roomify.model.ApprovalStatus;
import com.example.roomify.model.ResourceStatus;
import com.example.roomify.model.User;
import com.example.roomify.security.AuthorizationGuard;
import com.example.roomify.persistence.SystemLogger;

import java.util.List;

public class AdminService {

    private static AdminService instance;

    private ApprovalWorkflowService approvalWorkflowService = ApprovalWorkflowService.getInstance();
    private ResourceStatusManager resourceStatusManager = ResourceStatusManager.getInstance();

    private AdminService() {
    }

    public static AdminService getInstance() {
        if (instance == null) {
            instance = new AdminService();
        }
        return instance;
    }

    public void approveBooking(String approvalId, User adminUser)
            throws UnauthorizedAccessException, ResourceUnavailableException {
        requireAdminPermission(adminUser, "APPROVE_BOOKINGS");
        approvalWorkflowService.approveRequest(approvalId, adminUser);
    }

    public void rejectBooking(String approvalId, User adminUser, String reason)
            throws UnauthorizedAccessException {
        requireAdminPermission(adminUser, "APPROVE_BOOKINGS");
        approvalWorkflowService.rejectRequest(approvalId, adminUser, reason);
    }

    public void cancelBooking(String bookingId, User adminUser, String reason)
            throws UnauthorizedAccessException {

        AuthorizationGuard.requireAdmin(adminUser.getRole());

        System.out.println("Booking " + bookingId + " was cancelled by Admin "
                + adminUser.getName() + ". Reason: " + reason);
        SystemLogger.logAdminAction("Cancelled booking " + bookingId
                + " by " + adminUser.getName() + ". Reason: " + reason);
    }

    public void overrideDecision(String approvalId, User adminUser, ApprovalStatus newStatus, String reason)
            throws UnauthorizedAccessException {

        requireAdminPermission(adminUser, "APPROVE_BOOKINGS");

        Approval approval = approvalWorkflowService.findApprovalById(approvalId);
        if (approval == null) {
            System.out.println("Approval " + approvalId + " was not found.");
            return;
        }

        ApprovalStatus oldStatus = approval.getStatus();
        approval.setStatus(newStatus);
        approval.setReviewedByAdminId(adminUser.getUserId());
        approval.setComments("OVERRIDDEN from " + oldStatus + " to " + newStatus + ". Reason: " + reason);

        System.out.println(adminUser.getName() + " overrode " + approvalId
                + " from " + oldStatus + " to " + newStatus);
        SystemLogger.logAdminAction(adminUser.getName() + " overrode " + approvalId
                + " from " + oldStatus + " to " + newStatus + ". Reason: " + reason);
    }

    public void updateResourceStatus(String resourceId, ResourceStatus newStatus, User adminUser)
            throws UnauthorizedAccessException {
        requireAdminPermission(adminUser, "MANAGE_RESOURCES");
        resourceStatusManager.updateStatus(resourceId, newStatus, adminUser);
    }

    public List<Approval> getPendingApprovals() {
        return approvalWorkflowService.getPendingApprovals();
    }

    public List<Approval> getAllApprovals() {
        return approvalWorkflowService.getAllApprovals();
    }

    private Admin requireAdminPermission(User user, String permission) throws UnauthorizedAccessException {
        AuthorizationGuard.requireAdmin(user.getRole());

        Admin admin = (Admin) user;
        if (!admin.hasPermission(permission)) {
            throw new UnauthorizedAccessException(
                    admin.getName() + " does not have the \"" + permission + "\" permission.");
        }
        return admin;
    }
}