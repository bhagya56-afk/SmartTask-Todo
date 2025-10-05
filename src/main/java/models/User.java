package models;

import java.time.LocalDateTime;

/**
 * User - Abstract base class for all user types
 * Demonstrates OOP principle: Inheritance
 */
public abstract class User {
    // Common fields (protected = accessible by child classes)
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String hashedPassword;
    protected LocalDateTime createdAt;
    protected LocalDateTime lastLoginAt;
    protected boolean isActive;

    // Default constructor
    public User() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Parameterized constructor
    public User(String email, String firstName, String lastName, String hashedPassword) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
    }

    // Common methods inherited by all user types
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

    // Abstract methods - must be implemented by child classes
    public abstract String toJson();
    public abstract String toFileString();

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
