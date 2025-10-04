package models;

import java.time.LocalDateTime;

/**
 * Student - Represents a student user in the system
 * Demonstrates OOP principle: Encapsulation
 */
public class Student {
    private String email;
    private String firstName;
    private String lastName;
    private String studentId;
    private String major;
    private String hashedPassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean isActive;

    public Student() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public Student(String email, String firstName, String lastName,
                   String studentId, String major, String hashedPassword) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
        this.major = major;
        this.hashedPassword = hashedPassword;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Business Logic Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    // JSON conversion for frontend
    public String toJson() {
        return String.format(
                "{\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"," +
                        "\"studentId\":\"%s\",\"major\":\"%s\",\"createdAt\":\"%s\"," +
                        "\"lastLoginAt\":%s,\"isActive\":%b}",
                email, firstName, lastName, studentId, major,
                createdAt.toString(),
                lastLoginAt != null ? "\"" + lastLoginAt.toString() + "\"" : "null",
                isActive
        );
    }

    // File storage format
    public String toFileString() {
        return String.join("|",
                email != null ? email : "",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                studentId != null ? studentId : "",
                major != null ? major : "",
                hashedPassword != null ? hashedPassword : "",
                createdAt.toString(),
                lastLoginAt != null ? lastLoginAt.toString() : "null",
                String.valueOf(isActive)
        );
    }

    // Parse from file
    public static Student fromFileString(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 9) {
                Student student = new Student();
                student.setEmail(parts[0]);
                student.setFirstName(parts[1]);
                student.setLastName(parts[2]);
                student.setStudentId(parts[3]);
                student.setMajor(parts[4]);
                student.setHashedPassword(parts[5]);
                student.setCreatedAt(LocalDateTime.parse(parts[6]));
                if (!parts[7].equals("null")) {
                    student.setLastLoginAt(LocalDateTime.parse(parts[7]));
                }
                student.setActive(Boolean.parseBoolean(parts[8]));
                return student;
            }
        } catch (Exception e) {
            System.err.println("Error parsing student: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Student{email='%s', name='%s %s', studentId='%s', major='%s', active=%b}",
                email, firstName, lastName, studentId, major, isActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return email != null && email.equals(student.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}