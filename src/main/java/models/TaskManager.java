package models;

import utils.FileHandler;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskManager - Manages all task operations
 * Demonstrates OOP principles: Encapsulation, Single Responsibility
 */
public class TaskManager {
    private List<Task> tasks;
    private int nextId;
    private FileHandler fileHandler;
    private static final String TASKS_FILE = "data/tasks.txt";

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.fileHandler = new FileHandler();
        this.nextId = 1;
        loadTasks();
    }

    /**
     * Add a new task
     */
    public Task addTask(String title, String description, String category,
                        Task.Priority priority, LocalDateTime dueDate, String studentEmail) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (studentEmail == null || studentEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Student email cannot be empty");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }

        Task task = new Task(nextId++, title, description, category, priority, dueDate, studentEmail);
        tasks.add(task);
        saveTasks();
        return task;
    }

    /**
     * Get task by ID
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
        if (studentEmail == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .collect(Collectors.toList());
    }

    /**
     * Update an existing task
     */
    public boolean updateTask(int id, String title, String description, String category,
                              Task.Priority priority, LocalDateTime dueDate) {
        Task task = getTaskById(id);
        if (task != null) {
            if (title != null && !title.trim().isEmpty()) {
                task.setTitle(title);
            }
            if (description != null) {
                task.setDescription(description);
            }
            if (category != null) {
                task.setCategory(category);
            }
            if (priority != null) {
                task.setPriority(priority);
            }
            if (dueDate != null) {
                task.setDueDate(dueDate);
            }
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Delete a task
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

    /**
     * Toggle task completion status
     */
    public boolean toggleTaskCompletion(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            saveTasks();
            return true;
        }
        return false;
    }

    // Filtering Methods

    /**
     * Get tasks by category
     */
    public List<Task> getTasksByCategory(String studentEmail, String category) {
        if (studentEmail == null || category == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(task -> task.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by priority
     */
    public List<Task> getTasksByPriority(String studentEmail, Task.Priority priority) {
        if (studentEmail == null || priority == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Get completed tasks
     */
    public List<Task> getCompletedTasks(String studentEmail) {
        if (studentEmail == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    /**
     * Get pending tasks
     */
    public List<Task> getPendingTasks(String studentEmail) {
        if (studentEmail == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks(String studentEmail) {
        if (studentEmail == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks due today
     */
    public List<Task> getTasksDueToday(String studentEmail) {
        if (studentEmail == null) return Collections.emptyList();

        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(Task::isDueToday)
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Search tasks by title
     */
    public List<Task> searchTasksByTitle(String studentEmail, String searchTerm) {
        if (studentEmail == null || searchTerm == null) return Collections.emptyList();

        String lowerSearchTerm = searchTerm.toLowerCase();
        return tasks.stream()
                .filter(task -> task.getStudentEmail().equalsIgnoreCase(studentEmail))
                .filter(task -> task.getTitle().toLowerCase().contains(lowerSearchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Get task statistics
     */
    public TaskStats getTaskStats(String studentEmail) {
        if (studentEmail == null) {
            return new TaskStats(0, 0, 0, 0, 0);
        }

        List<Task> studentTasks = getTasksByStudent(studentEmail);

        int total = studentTasks.size();
        int completed = (int) studentTasks.stream().filter(Task::isCompleted).count();
        int pending = total - completed;
        int overdue = (int) studentTasks.stream().filter(Task::isOverdue).count();
        int dueToday = (int) studentTasks.stream()
                .filter(Task::isDueToday)
                .filter(task -> !task.isCompleted())
                .count();

        return new TaskStats(total, completed, pending, overdue, dueToday);
    }

    /**
     * Get tasks sorted by due date
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
     * Get tasks sorted by priority
     */
    public List<Task> getTasksSortedByPriority(String studentEmail) {
        Map<Task.Priority, Integer> priorityOrder = Map.of(
                Task.Priority.HIGH, 3,
                Task.Priority.MEDIUM, 2,
                Task.Priority.LOW, 1
        );

        return getTasksByStudent(studentEmail).stream()
                .sorted((t1, t2) ->
                        priorityOrder.get(t2.getPriority()).compareTo(priorityOrder.get(t1.getPriority()))
                )
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
            System.out.println("Loaded " + tasks.size() + " tasks.");
        } catch (Exception e) {
            System.out.println("Starting with empty task database.");
        }
    }

    /**
     * Save tasks to file
     */
    private boolean saveTasks() {
        try {
            List<String> lines = tasks.stream()
                    .map(this::taskToString)
                    .collect(Collectors.toList());
            fileHandler.writeFile(TASKS_FILE, lines);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convert task to string for file storage
     */
    private String taskToString(Task task) {
        return String.join("|",
                String.valueOf(task.getId()),
                task.getTitle() != null ? task.getTitle() : "",
                task.getDescription() != null ? task.getDescription() : "",
                task.getCategory() != null ? task.getCategory() : "",
                task.getPriority().getValue(),
                task.getDueDate().toString(),
                String.valueOf(task.isCompleted()),
                task.getCreatedAt().toString(),
                task.getCompletedAt() != null ? task.getCompletedAt().toString() : "null",
                task.getStudentEmail() != null ? task.getStudentEmail() : ""
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
            System.err.println("Error parsing task: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inner class for task statistics
     */
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