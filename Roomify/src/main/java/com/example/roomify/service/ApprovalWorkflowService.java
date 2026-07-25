package com.example.roomify.service;

import com.example.roomify.exception.ResourceUnavailableException;
import com.example.roomify.exception.UnauthorizedAccessException;
import com.example.roomify.model.Approval;
import com.example.roomify.model.ApprovalStatus;
import com.example.roomify.model.User;
import com.example.roomify.security.AuthorizationGuard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApprovalWorkflowService {

    private static ApprovalWorkflowService instance;
    private List<Approval> approvalList;
    private int nextApprovalNumber = 1;

    // Needed to double-check a resource is still available before approving it.
    private ResourceStatusManager resourceStatusManager = ResourceStatusManager.getInstance();

    private ApprovalWorkflowService() {
        approvalList = new ArrayList<>();
    }

    public static ApprovalWorkflowService getInstance() {
        if (instance == null) {
            instance = new ApprovalWorkflowService();
        }
        return instance;
    }

    public Approval submitForApproval(String bookingId, String resourceId, String requestedByUserId) {
        String approvalId = "APR-" + nextApprovalNumber;
        nextApprovalNumber++;

        Approval approval = new Approval(approvalId, bookingId, resourceId, requestedByUserId);
        approvalList.add(approval);

        System.out.println("New approval request created: " + approvalId + " for booking " + bookingId);
        return approval;
    }

    /**
     * Admin approves a pending request.
     * adminUser is checked here so this method is safe to call on its own;
     * AdminService additionally checks the finer-grained permission before
     * it ever gets here.
     */
    public void approveRequest(String approvalId, User adminUser)
            throws UnauthorizedAccessException, ResourceUnavailableException {

        AuthorizationGuard.requireAdmin(adminUser.getRole());

        Approval approval = findApprovalById(approvalId);
        if (approval == null) {
            System.out.println("Approval " + approvalId + " was not found.");
            return;
        }

        // Make sure the resource hasn't been taken offline since the request was made.
        if (!resourceStatusManager.isAvailable(approval.getResourceId())) {
            throw new ResourceUnavailableException(
                    "Cannot approve booking " + approval.getBookingId() + " because resource "
                            + approval.getResourceId() + " is not available right now.");
        }

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setReviewedByAdminId(adminUser.getUserId());
        approval.setReviewDate(LocalDateTime.now());

        System.out.println(approvalId + " approved by " + adminUser.getName());
    }

    /**
     * Admin rejects a pending request and records why.
     */
    public void rejectRequest(String approvalId, User adminUser, String reason)
            throws UnauthorizedAccessException {

        AuthorizationGuard.requireAdmin(adminUser.getRole());

        Approval approval = findApprovalById(approvalId);
        if (approval == null) {
            System.out.println("Approval " + approvalId + " was not found.");
            return;
        }

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setReviewedByAdminId(adminUser.getUserId());
        approval.setReviewDate(LocalDateTime.now());
        approval.setComments(reason);

        System.out.println(approvalId + " rejected by " + adminUser.getName() + ". Reason: " + reason);
    }

    /**
     * Finds an approval by its ID.
     *
     * @param approvalId The approval ID to search for
     * @return The Approval object if found, null otherwise
     */
    public Approval findApprovalById(String approvalId) {
        for (Approval approval : approvalList) {
            if (approval.getApprovalId().equals(approvalId)) {
                return approval;
            }
        }
        return null;
    }

    /**
     * Gets all pending approvals.
     *
     * @return List of approvals with PENDING status
     */
    public List<Approval> getPendingApprovals() {
        List<Approval> pendingApprovals = new ArrayList<>();
        for (Approval approval : approvalList) {
            if (approval.getStatus() == ApprovalStatus.PENDING) {
                pendingApprovals.add(approval);
            }
        }
        return pendingApprovals;
    }

    /**
     * Gets all approvals.
     *
     * @return List of all approvals
     */
    public List<Approval> getAllApprovals() {
        return new ArrayList<>(approvalList);
    }
}