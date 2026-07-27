package com.example.roomify.model;

import com.example.roomify.UserRole;

public class Staff extends User {
    private static final long serialVersionUID = 1L;

    private String staffId;
    private String department;

    public Staff(String userId, String name, String email, String password, String staffId, String department) {
        super(userId, name, email, password, UserRole.STAFF);
        this.staffId = staffId;
        this.department = department;
    }

    @Override
    public String toString() {
        return "Staff Member: " + getName() + " [Dept: " + department + "]";
    }

    // Getters and Setters
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}