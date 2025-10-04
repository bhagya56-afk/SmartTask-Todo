import models.*;
import auth.AuthManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * SmartTaskApp - Main application class
 * Demonstrates OOP principles: Encapsulation, Abstraction
 */
public class SmartTaskApp {
    private AuthManager authManager;
    private TaskManager taskManager;
    private Student currentStudent;
    private Scanner scanner;
    private boolean isRunning;

    public SmartTaskApp() {
        this.authManager = new AuthManager();
        this.taskManager = new TaskManager();
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
    }

    public static void main(String[] args) {
        SmartTaskApp app = new SmartTaskApp();
        app.start();
    }

    /**
     * Start the application
     */
    public void start() {
        System.out.println("=================================");
        System.out.println("    Welcome to SmartTask App    ");
        System.out.println("=================================");

        try {
            while (isRunning) {
                if (currentStudent == null) {
                    showLoginMenu();
                } else {
                    showMainMenu();
                }
            }
        } finally {
            scanner.close();
            System.out.println("Thank you for using SmartTask!");
        }
    }

    /**
     * Display login/registration menu
     */
    private void showLoginMenu() {
        System.out.println("\n--- Authentication Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = getIntegerInput();

        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleRegistration();
                break;
            case 3:
                isRunning = false;
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * Display main application menu
     */
    private void showMainMenu() {
        System.out.println("\n=== SmartTask Dashboard ===");
        System.out.println("Welcome, " + currentStudent.getFullName() + "!");

        // Display quick stats
        TaskManager.TaskStats stats = taskManager.getTaskStats(currentStudent.getEmail());
        System.out.printf("Tasks: %d Total | %d Completed | %d Pending | %d Due Today%n",
                stats.getTotal(), stats.getCompleted(), stats.getPending(), stats.getDueToday());

        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View All Tasks");
        System.out.println("2. Add New Task");
        System.out.println("3. Complete Task");
        System.out.println("4. Edit Task");
        System.out.println("5. Delete Task");
        System.out.println("6. View Tasks by Category");
        System.out.println("7. View Tasks by Priority");
        System.out.println("8. View Overdue Tasks");
        System.out.println("9. Search Tasks");
        System.out.println("10. Profile Settings");
        System.out.println("11. Logout");
        System.out.print("Choose an option: ");

        int choice = getIntegerInput();

        switch (choice) {
            case 1: viewAllTasks(); break;
            case 2: addNewTask(); break;
            case 3: completeTask(); break;
            case 4: editTask(); break;
            case 5: deleteTask(); break;
            case 6: viewTasksByCategory(); break;
            case 7: viewTasksByPriority(); break;
            case 8: viewOverdueTasks(); break;
            case 9: searchTasks(); break;
            case 10: showProfile(); break;
            case 11: logout(); break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * Handle user login
     */
    private void handleLogin() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        Student student = authManager.login(email, password);
        if (student != null) {
            currentStudent = student;
            System.out.println("Login successful! Welcome back, " + student.getFirstName());
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    /**
     * Handle user registration
     */
    private void handleRegistration() {
        System.out.println("\n--- Student Registration ---");

        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (!AuthManager.isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }

        if (authManager.emailExists(email)) {
            System.out.println("Email already registered.");
            return;
        }

        System.out.print("Student ID: ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Major: ");
        String major = scanner.nextLine().trim();

        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine().trim();

        if (!AuthManager.isValidPassword(password)) {
            System.out.println("Password must be at least 6 characters.");
            return;
        }

        if (authManager.register(firstName, lastName, email, studentId, major, password)) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    /**
     * View all tasks
     */
    private void viewAllTasks() {
        List<Task> tasks = taskManager.getTasksByStudent(currentStudent.getEmail());
        displayTasks(tasks, "All Tasks");
    }

    /**
     * Add a new task
     */
    private void addNewTask() {
        System.out.println("\n--- Add New Task ---");

        System.out.print("Task Title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Category (study/personal/lab/project/exam/event/other): ");
        String category = scanner.nextLine().trim();

        System.out.print("Priority (high/medium/low): ");
        String priorityStr = scanner.nextLine().trim();
        Task.Priority priority = Task.Priority.fromString(priorityStr);

        System.out.print("Due Date (YYYY-MM-DD): ");
        String dateStr = scanner.nextLine().trim();

        System.out.print("Due Time (HH:MM) [optional, press Enter to skip]: ");
        String timeStr = scanner.nextLine().trim();

        try {
            LocalDateTime dueDate;
            if (timeStr.isEmpty()) {
                dueDate = LocalDateTime.parse(dateStr + "T23:59:59");
            } else {
                dueDate = LocalDateTime.parse(dateStr + "T" + timeStr + ":00");
            }

            Task newTask = taskManager.addTask(title, description, category, priority, dueDate, currentStudent.getEmail());
            System.out.println("Task added successfully! ID: " + newTask.getId());

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD and HH:MM.");
        } catch (Exception e) {
            System.out.println("Error adding task: " + e.getMessage());
        }
    }

    /**
     * Mark task as completed
     */
    private void completeTask() {
        List<Task> pendingTasks = taskManager.getPendingTasks(currentStudent.getEmail());
        if (pendingTasks.isEmpty()) {
            System.out.println("No pending tasks found.");
            return;
        }

        displayTasks(pendingTasks, "Pending Tasks");

        System.out.print("Enter task ID to complete: ");
        int taskId = getIntegerInput();

        Task task = taskManager.getTaskById(taskId);
        if (task == null || !task.getStudentEmail().equals(currentStudent.getEmail())) {
            System.out.println("Task not found.");
            return;
        }

        if (taskManager.completeTask(taskId)) {
            System.out.println("Task completed successfully!");
        } else {
            System.out.println("Failed to complete task.");
        }
    }

    /**
     * Edit task
     */
    private void editTask() {
        List<Task> tasks = taskManager.getTasksByStudent(currentStudent.getEmail());
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        displayTasks(tasks, "Your Tasks");

        System.out.print("Enter task ID to edit: ");
        int taskId = getIntegerInput();

        Task task = taskManager.getTaskById(taskId);
        if (task == null || !task.getStudentEmail().equals(currentStudent.getEmail())) {
            System.out.println("Task not found.");
            return;
        }

        System.out.println("\nEditing task: " + task.getTitle());
        System.out.println("Press Enter to keep current value");

        System.out.print("New Title [" + task.getTitle() + "]: ");
        String title = getStringInputOrDefault(task.getTitle());

        System.out.print("New Description [" + task.getDescription() + "]: ");
        String description = getStringInputOrDefault(task.getDescription());

        System.out.print("New Category [" + task.getCategory() + "]: ");
        String category = getStringInputOrDefault(task.getCategory());

        System.out.print("New Priority [" + task.getPriority().getValue() + "]: ");
        String priorityStr = getStringInputOrDefault(task.getPriority().getValue());
        Task.Priority priority = Task.Priority.fromString(priorityStr);

        System.out.print("New Due Date [" + task.getDueDate().toLocalDate() + "]: ");
        String dateStr = getStringInputOrDefault(task.getDueDate().toLocalDate().toString());

        try {
            LocalDateTime dueDate = LocalDateTime.parse(dateStr + "T" + task.getDueDate().toLocalTime());

            if (taskManager.updateTask(taskId, title, description, category, priority, dueDate)) {
                System.out.println("Task updated successfully!");
            } else {
                System.out.println("Failed to update task.");
            }
        } catch (Exception e) {
            System.out.println("Error updating task: " + e.getMessage());
        }
    }

    /**
     * Delete task
     */
    private void deleteTask() {
        List<Task> tasks = taskManager.getTasksByStudent(currentStudent.getEmail());
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        displayTasks(tasks, "Your Tasks");

        System.out.print("Enter task ID to delete: ");
        int taskId = getIntegerInput();

        Task task = taskManager.getTaskById(taskId);
        if (task == null || !task.getStudentEmail().equals(currentStudent.getEmail())) {
            System.out.println("Task not found.");
            return;
        }

        System.out.print("Are you sure you want to delete '" + task.getTitle() + "'? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("y") || confirmation.equals("yes")) {
            if (taskManager.deleteTask(taskId)) {
                System.out.println("Task deleted successfully!");
            } else {
                System.out.println("Failed to delete task.");
            }
        }
    }

    /**
     * View tasks by category
     */
    private void viewTasksByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();

        List<Task> tasks = taskManager.getTasksByCategory(currentStudent.getEmail(), category);
        displayTasks(tasks, "Tasks in category: " + category);
    }

    /**
     * View tasks by priority
     */
    private void viewTasksByPriority() {
        System.out.print("Enter priority (high/medium/low): ");
        String priorityStr = scanner.nextLine().trim();
        Task.Priority priority = Task.Priority.fromString(priorityStr);

        List<Task> tasks = taskManager.getTasksByPriority(currentStudent.getEmail(), priority);
        displayTasks(tasks, priority.getValue().toUpperCase() + " Priority Tasks");
    }

    /**
     * View overdue tasks
     */
    private void viewOverdueTasks() {
        List<Task> tasks = taskManager.getOverdueTasks(currentStudent.getEmail());
        displayTasks(tasks, "Overdue Tasks");
    }

    /**
     * Search tasks
     */
    private void searchTasks() {
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().trim();

        List<Task> tasks = taskManager.searchTasksByTitle(currentStudent.getEmail(), searchTerm);
        displayTasks(tasks, "Search Results for: " + searchTerm);
    }

    /**
     * Show profile
     */
    private void showProfile() {
        System.out.println("\n=== Profile Information ===");
        System.out.println("Name: " + currentStudent.getFullName());
        System.out.println("Email: " + currentStudent.getEmail());
        System.out.println("Student ID: " + currentStudent.getStudentId());
        System.out.println("Major: " + currentStudent.getMajor());
        System.out.println("Member since: " + currentStudent.getCreatedAt().toLocalDate());

        TaskManager.TaskStats stats = taskManager.getTaskStats(currentStudent.getEmail());
        System.out.println("\n=== Task Statistics ===");
        System.out.println("Total Tasks: " + stats.getTotal());
        System.out.println("Completed: " + stats.getCompleted());
        System.out.println("Pending: " + stats.getPending());
        System.out.println("Overdue: " + stats.getOverdue());
        System.out.println("Due Today: " + stats.getDueToday());
    }

    /**
     * Logout
     */
    private void logout() {
        System.out.println("Goodbye, " + currentStudent.getFirstName() + "!");
        currentStudent = null;
    }

    /**
     * Display tasks in formatted table
     */
    private void displayTasks(List<Task> tasks, String title) {
        System.out.println("\n=== " + title + " ===");

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        System.out.printf("%-4s %-30s %-15s %-10s %-20s %-10s%n",
                "ID", "Title", "Category", "Priority", "Due Date", "Status");
        System.out.println("=".repeat(95));

        for (Task task : tasks) {
            String status = task.isCompleted() ? "✓ Done" :
                    task.isOverdue() ? "⚠ Overdue" :
                            task.isDueToday() ? "⏰ Today" : "Pending";

            String displayTitle = task.getTitle().length() > 30
                    ? task.getTitle().substring(0, 27) + "..."
                    : task.getTitle();

            System.out.printf("%-4d %-30s %-15s %-10s %-20s %-10s%n",
                    task.getId(),
                    displayTitle,
                    task.getCategory(),
                    task.getPriority().getValue().toUpperCase(),
                    task.getDueDate().format(formatter),
                    status);
        }
    }

    /**
     * Get integer input with validation
     */
    private int getIntegerInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Get string input or return default
     */
    private String getStringInputOrDefault(String defaultValue) {
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
}