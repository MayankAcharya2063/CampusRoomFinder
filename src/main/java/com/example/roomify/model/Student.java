package com.example.roomify.model;

import com.example.roomify.UserRole;


public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String department;

    public Student(String userId, String name, String email, String password, String studentId, String department) {
        super(userId, name, email, password, UserRole.STUDENT);
        this.studentId = studentId;
        this.department = department;
    }

    // Polymorphic display or behavior adjustments can go here
    @Override
    public String toString() {
        return "Student: " + getName() + " [Dept: " + department + "]";
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}