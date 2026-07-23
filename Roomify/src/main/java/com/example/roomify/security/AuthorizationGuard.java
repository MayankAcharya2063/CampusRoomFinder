package com.example.roomify.security;

import com.example.roomify.UserRole;
import com.example.roomify.exception.UnauthorizedAccessException;

/**
 * Global role-based access control helper.
 */
public class AuthorizationGuard {

    public static void requireAdmin(UserRole role)
            throws UnauthorizedAccessException {

        if (role != UserRole.ADMIN) {
            throw new UnauthorizedAccessException(
                    "Access denied. Admin privileges required.");
        }
    }

    public static void requireStaffOrAdmin(UserRole role)
            throws UnauthorizedAccessException {

        if (role != UserRole.ADMIN && role != UserRole.STAFF) {
            throw new UnauthorizedAccessException(
                    "Access denied. Staff or Admin privileges required.");
        }
    }
}