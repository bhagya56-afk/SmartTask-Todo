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
    // Private attributes (Encapsulation)
    private List<Student> students;
    private FileHandler fileHandler;
    private static final String STUDENTS_FILE = "data/students.txt";

    // Constructor
    public AuthManager() {
        this.students = new ArrayList<>();
        this.fileHandler = new FileHandler();
        loadStudents();
    }

    /**
     * Register a new student
     * @param firstName Student's first name
     * @param lastName Student's last name
     * @param email Student's email (unique identifier)
     * @param studentId Student's ID
     * @param major Student's major
     * @param password Plain text password (will be hashed)
     * @return true if registration successful, false if email already exists
     */
    public boolean register(String firstName, String lastName, String email,
                            String studentId, String major, String password) {
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
        saveStudents();

        return true;
    }

    /**
     * Login a student
     * @param email Student's email
     * @param password Plain text password
     * @return Student object if login successful, null otherwise
     */
    public Student login(String email, String password) {
        Student student = findStudentByEmail(email);

        if (student != null && student.isActive()) {
            String hashedPassword = hashPassword(password);
            if (hashedPassword.equals(student.getHashedPassword())) {
                student.updateLastLogin();
                saveStudents(); // Save updated login time
                return student;
            }
        }

        return null;
    }

    /**
     * Change student password
     * @param email Student's email
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     */
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        Student student = findStudentByEmail(email);

        if (student != null) {
            String hashedOldPassword = hashPassword(oldPassword);
            if (hashedOldPassword.equals(student.getHashedPassword())) {
                String hashedNewPassword = hashPassword(newPassword);
                student.setHashedPassword(hashedNewPassword);
                saveStudents();
                return true;
            }
        }

        return false;
    }

    /**
     * Update student profile information
     * @param email Student's email
     * @param firstName New first name
     * @param lastName New last name
     * @param major New major
     * @return true if update successful
     */
    public boolean updateProfile(String email, String firstName, String lastName, String major) {
        Student student = findStudentByEmail(email);

        if (student != null) {
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setMajor(major);
            saveStudents();
            return true;
        }

        return false;
    }

    /**
     * Deactivate student account
     * @param email Student's email
     * @return true if deactivated successfully
     */
    public boolean deactivateAccount(String email) {
        Student student = findStudentByEmail(email);

        if (student != null) {
            student.deactivate();
            saveStudents();
            return true;
        }

        return false;
    }

    /**
     * Get all students (for admin purposes)
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(students); // Return copy for security
    }

    /**
     * Get student by email
     * @param email Student's email
     * @return Student object or null if not found
     */
    public Student getStudentByEmail(String email) {
        return findStudentByEmail(email);
    }

    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        return findStudentByEmail(email) != null;
    }

    // Private helper methods

    /**
     * Find student by email (internal method)
     * @param email Email to search for
     * @return Student object or null if not found
     */
    private Student findStudentByEmail(String email) {
        return students.stream()
                .filter(student -> student.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    /**
     * Hash password using SHA-256
     * @param password Plain text password
     * @return Hashed password
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
            System.out.println("Loaded " + students.size() + " students from file.");
        } catch (Exception e) {
            System.out.println("Could not load students: " + e.getMessage());
            System.out.println("Starting with empty student database.");
        }
    }

    /**
     * Save students to file
     */
    private void saveStudents() {
        try {
            List<String> lines = new ArrayList<>();
            for (Student student : students) {
                lines.add(student.toFileString());
            }
            fileHandler.writeFile(STUDENTS_FILE, lines);
        } catch (Exception e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    /**
     * Validate email format (basic validation)
     * @param email Email to validate
     * @return true if email format is valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Get user statistics
     * @return String with user statistics
     */
    public String getUserStats() {
        long totalUsers = students.size();
        long activeUsers = students.stream().filter(Student::isActive).count();
        long inactiveUsers = totalUsers - activeUsers;

        return String.format("Total Users: %d | Active: %d | Inactive: %d",
                totalUsers, activeUsers, inactiveUsers);
    }
}