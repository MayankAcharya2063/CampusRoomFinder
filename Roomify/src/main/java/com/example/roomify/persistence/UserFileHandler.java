package com.example.roomify.persistence;

import com.example.roomify.model.User;

import java.util.List;

/**
 * Handles saving and loading user accounts.
 */
public class UserFileHandler {

    // Binary file for user accounts
    private static final String USER_FILE = "users.dat";

    /**
     * Save all users.
     */
    public static void saveUsers(List<User> users) {
        FilePersistenceEngine.saveObjects(users, USER_FILE);
    }

    /**
     * Load all users.
     */
    @SuppressWarnings("unchecked")
    public static List<User> loadUsers() {
        return FilePersistenceEngine.loadObjects(USER_FILE);
    }

    /**
     * Save users to text file (alternative format for readability).
     */
    public static void saveUsersToText(List<User> users) {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append(user.getUserId()).append("|")
                    .append(user.getName()).append("|")
                    .append(user.getEmail()).append("|")
                    .append(user.getPassword()).append("|")
                    .append(user.getRole().name()).append("|");

            // Handle subclass-specific fields
            if (user instanceof com.example.roomify.model.Student) {
                com.example.roomify.model.Student student = (com.example.roomify.model.Student) user;
                sb.append("STUDENT|")
                        .append(student.getStudentId()).append("|")
                        .append(student.getDepartment());
            } else if (user instanceof com.example.roomify.model.Staff) {
                com.example.roomify.model.Staff staff = (com.example.roomify.model.Staff) user;
                sb.append("STAFF|")
                        .append(staff.getStaffId()).append("|")
                        .append(staff.getDepartment());
            } else if (user instanceof com.example.roomify.model.Admin) {
                com.example.roomify.model.Admin admin = (com.example.roomify.model.Admin) user;
                sb.append("ADMIN|")
                        .append(admin.getAdminLevel());
            }
            sb.append("\n");
        }
        FilePersistenceEngine.writeText("users.txt", sb.toString());
    }
}