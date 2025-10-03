package models;

import utils.FileHandler;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    // Private attributes (Encapsulation)
    private List<Task> tasks;
    private int nextId;
    private FileHandler fileHandler;
    private static final String TASKS_FILE = "data/tasks.txt";

    // Singleton Pattern (Optional - if you want only one TaskManager instance)
    private static TaskManager instance;

    // Constructor
    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.fileHandler = new FileHandler();
        this.nextId = 1;
        loadTasks();
    }

    // Singleton getInstance method (Optional)
    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    // Task CRUD Operations

    /**
     * Add a new task (Create)
     */
    public Task addTask(String title, String description, String category,
                        Task.Priority priority, LocalDateTime dueDate, String studentEmail) {
        Task task = new Task(nextId++, title, description, category, priority, dueDate, studentEmail);
        tasks.add(task);
        saveTasks();
        return task;
    }

    /**
     * Get task by ID (Read)
     */
    public Task getTaskById(int id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all tasks for a specific student
     */
    public List<Task> getTasksByStudent(String studentEmail) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .collect(Collectors.toList());
    }

    /**
     * Update an existing task (Update)
     */
    public boolean updateTask(int id, String title, String description, String category,
                              Task.Priority priority, LocalDateTime dueDate) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setTitle(title);
            task.setDescription(description);
            task.setCategory(category);
            task.setPriority(priority);
            task.setDueDate(dueDate);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Delete a task (Delete)
     */
    public boolean deleteTask(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            tasks.remove(task);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Mark task as completed
     */
    public boolean completeTask(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.markCompleted();
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Mark task as pending
     */
    public boolean markTaskPending(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.markPending();
            saveTasks();
            return true;
        }
        return false;
    }

    // Filtering and Searching Methods

    /**
     * Get tasks by category for a specific student
     */
    public List<Task> getTasksByCategory(String studentEmail, String category) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(task -> task.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by priority for a specific student
     */
    public List<Task> getTasksByPriority(String studentEmail, Task.Priority priority) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Get completed tasks for a specific student
     */
    public List<Task> getCompletedTasks(String studentEmail) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    /**
     * Get pending tasks for a specific student
     */
    public List<Task> getPendingTasks(String studentEmail) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Get overdue tasks for a specific student
     */
    public List<Task> getOverdueTasks(String studentEmail) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks due today for a specific student
     */
    public List<Task> getTasksDueToday(String studentEmail) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(Task::isDueToday)
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Search tasks by title for a specific student
     */
    public List<Task> searchTasksByTitle(String studentEmail, String searchTerm) {
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equals(studentEmail))
                .filter(task -> task.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Statistics Methods

    /**
     * Get task statistics for a student
     */
    public TaskStats getTaskStats(String studentEmail) {
        List<Task> studentTasks = getTasksByStudent(studentEmail);

        int total = studentTasks.size();
        int completed = (int) studentTasks.stream().filter(Task::isCompleted).count();
        int pending = total - completed;
        int overdue = (int) studentTasks.stream().filter(Task::isOverdue).count();
        int dueToday = (int) studentTasks.stream().filter(Task::isDueToday).count();

        return new TaskStats(total, completed, pending, overdue, dueToday);
    }

    /**
     * Get tasks sorted by due date for a student
     */
    public List<Task> getTasksSortedByDueDate(String studentEmail, boolean ascending) {
        List<Task> studentTasks = getTasksByStudent(studentEmail);
        if (ascending) {
            return studentTasks.stream()
                    .sorted(Comparator.comparing(Task::getDueDate))
                    .collect(Collectors.toList());
        } else {
            return studentTasks.stream()
                    .sorted(Comparator.comparing(Task::getDueDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get tasks sorted by priority for a student
     */
    public List<Task> getTasksSortedByPriority(String studentEmail) {
        return getTasksByStudent(studentEmail).stream()
                .sorted((t1, t2) -> {
                    // High priority first, then Medium, then Low
                    Map<Task.Priority, Integer> priorityOrder = Map.of(
                            Task.Priority.HIGH, 3,
                            Task.Priority.MEDIUM, 2,
                            Task.Priority.LOW, 1
                    );
                    return priorityOrder.get(t2.getPriority()).compareTo(priorityOrder.get(t1.getPriority()));
                })
                .collect(Collectors.toList());
    }

    // File Operations

    /**
     * Load tasks from file
     */
    private void loadTasks() {
        try {
            List<String> lines = fileHandler.readFile(TASKS_FILE);
            for (String line : lines) {
                Task task = parseTaskFromString(line);
                if (task != null) {
                    tasks.add(task);
                    if (task.getId() >= nextId) {
                        nextId = task.getId() + 1;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load tasks: " + e.getMessage());
        }
    }

    /**
     * Save tasks to file
     */
    private void saveTasks() {
        try {
            List<String> lines = tasks.stream()
                    .map(this::taskToString)
                    .collect(Collectors.toList());
            fileHandler.writeFile(TASKS_FILE, lines);
        } catch (Exception e) {
            System.out.println("Could not save tasks: " + e.getMessage());
        }
    }

    /**
     * Convert task to string for file storage
     */
    private String taskToString(Task task) {
        return String.join("|",
                String.valueOf(task.getId()),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority().getValue(),
                task.getDueDate().toString(),
                String.valueOf(task.isCompleted()),
                task.getCreatedAt().toString(),
                task.getCompletedAt() != null ? task.getCompletedAt().toString() : "null",
                task.getStudentEmail()
        );
    }

    /**
     * Parse task from string
     */
    private Task parseTaskFromString(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 10) {
                Task task = new Task();
                task.setId(Integer.parseInt(parts[0]));
                task.setTitle(parts[1]);
                task.setDescription(parts[2]);
                task.setCategory(parts[3]);
                task.setPriority(Task.Priority.fromString(parts[4]));
                task.setDueDate(LocalDateTime.parse(parts[5]));
                task.setCompleted(Boolean.parseBoolean(parts[6]));
                task.setCreatedAt(LocalDateTime.parse(parts[7]));
                if (!parts[8].equals("null")) {
                    task.setCompletedAt(LocalDateTime.parse(parts[8]));
                }
                task.setStudentEmail(parts[9]);
                return task;
            }
        } catch (Exception e) {
            System.out.println("Error parsing task: " + line + " - " + e.getMessage());
        }
        return null;
    }

    // Inner class for task statistics
    public static class TaskStats {
        private final int total;
        private final int completed;
        private final int pending;
        private final int overdue;
        private final int dueToday;

        public TaskStats(int total, int completed, int pending, int overdue, int dueToday) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
            this.dueToday = dueToday;
        }

        // Getters
        public int getTotal() { return total; }
        public int getCompleted() { return completed; }
        public int getPending() { return pending; }
        public int getOverdue() { return overdue; }
        public int getDueToday() { return dueToday; }

        public String toJson() {
            return String.format(
                    "{\"total\":%d,\"completed\":%d,\"pending\":%d,\"overdue\":%d,\"dueToday\":%d}",
                    total, completed, pending, overdue, dueToday
            );
        }

        @Override
        public String toString() {
            return String.format("TaskStats{total=%d, completed=%d, pending=%d, overdue=%d, dueToday=%d}",
                    total, completed, pending, overdue, dueToday);
        }
    }
}