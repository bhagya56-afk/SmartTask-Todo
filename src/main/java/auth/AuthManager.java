package auth;

import models.Student;
import utils.FileHandler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthManager - Handles user authentication and registration
 * Demonstrates OOP principles: Encapsulation, Single Responsibility
 */
public class AuthManager {
    private List<Student> students;
    private FileHandler fileHandler;
    private static final String STUDENTS_FILE = "data/students.txt";

    public AuthManager() {
        this.students = new ArrayList<>();
        this.fileHandler = new FileHandler();
        loadStudents();
    }

    /**
     * Register a new student
     */
    public boolean register(String firstName, String lastName, String email,
                            String studentId, String major, String password) {
        // Validate inputs
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return false;
        }

        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            return false;
        }

        // Check if email already exists
        if (findStudentByEmail(email) != null) {
            return false;
        }

        // Hash the password for security
        String hashedPassword = hashPassword(password);

        // Create new student
        Student student = new Student(email, firstName, lastName, studentId, major, hashedPassword);
        students.add(student);

        // Save to file
        return saveStudents();
    }

    /**
     * Login a student
     */
    public Student login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        Student student = findStudentByEmail(email);

        if (student != null && student.isActive()) {
            String hashedPassword = hashPassword(password);
            if (hashedPassword.equals(student.getHashedPassword())) {
                student.updateLastLogin();
                saveStudents();
                return student;
            }
        }

        return null;
    }

    /**
     * Change student password
     */
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        if (!isValidPassword(newPassword)) {
            return false;
        }

        Student student = findStudentByEmail(email);

        if (student != null) {
            String hashedOldPassword = hashPassword(oldPassword);
            if (hashedOldPassword.equals(student.getHashedPassword())) {
                String hashedNewPassword = hashPassword(newPassword);
                student.setHashedPassword(hashedNewPassword);
                return saveStudents();
            }
        }

        return false;
    }

    /**
     * Update student profile information
     */
    public boolean updateProfile(String email, String firstName, String lastName, String major) {
        Student student = findStudentByEmail(email);

        if (student != null) {
            if (firstName != null && !firstName.trim().isEmpty()) {
                student.setFirstName(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                student.setLastName(lastName.trim());
            }
            if (major != null && !major.trim().isEmpty()) {
                student.setMajor(major.trim());
            }
            return saveStudents();
        }

        return false;
    }

    /**
     * Deactivate student account
     */
    public boolean deactivateAccount(String email) {
        Student student = findStudentByEmail(email);

        if (student != null) {
            student.deactivate();
            return saveStudents();
        }

        return false;
    }

    /**
     * Get all students (for admin purposes)
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    /**
     * Get student by email
     */
    public Student getStudentByEmail(String email) {
        return findStudentByEmail(email);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return findStudentByEmail(email) != null;
    }

    /**
     * Find student by email (internal method)
     */
    private Student findStudentByEmail(String email) {
        if (email == null) return null;

        return students.stream()
                .filter(student -> student.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Load students from file
     */
    private void loadStudents() {
        try {
            List<String> lines = fileHandler.readFile(STUDENTS_FILE);
            for (String line : lines) {
                Student student = Student.fromFileString(line);
                if (student != null) {
                    students.add(student);
                }
            }
            System.out.println("Loaded " + students.size() + " students.");
        } catch (Exception e) {
            System.out.println("Starting with empty student database.");
        }
    }

    /**
     * Save students to file
     */
    private boolean saveStudents() {
        try {
            List<String> lines = new ArrayList<>();
            for (Student student : students) {
                lines.add(student.toFileString());
            }
            fileHandler.writeFile(STUDENTS_FILE, lines);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving students: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Get user statistics
     */
    public String getUserStats() {
        long totalUsers = students.size();
        long activeUsers = students.stream().filter(Student::isActive).count();
        long inactiveUsers = totalUsers - activeUsers;

        return String.format("Total Users: %d | Active: %d | Inactive: %d",
                totalUsers, activeUsers, inactiveUsers);
    }
}