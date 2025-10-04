package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Task - Represents a task in the system
 * Demonstrates OOP principles: Encapsulation, Enums
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private String category;
    private Priority priority;
    private LocalDateTime dueDate;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String studentEmail;

    public enum Priority {
        HIGH("high"), MEDIUM("medium"), LOW("low");

        private final String value;

        Priority(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Priority fromString(String priority) {
            if (priority == null) return MEDIUM;

            for (Priority p : Priority.values()) {
                if (p.value.equalsIgnoreCase(priority)) {
                    return p;
                }
            }
            return MEDIUM;
        }
    }

    public Task() {
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    public Task(String title, String description, String category,
                Priority priority, LocalDateTime dueDate, String studentEmail) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.studentEmail = studentEmail;
    }

    public Task(int id, String title, String description, String category,
                Priority priority, LocalDateTime dueDate, String studentEmail) {
        this(title, description, category, priority, dueDate, studentEmail);
        this.id = id;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    // Business Logic
    public void markCompleted() {
        setCompleted(true);
    }

    public void markPending() {
        setCompleted(false);
    }

    public boolean isOverdue() {
        return !completed && dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    public boolean isDueToday() {
        if (dueDate == null) return false;
        return dueDate.toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
    }

    public long getDaysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    // JSON conversion
    public String toJson() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\"," +
                        "\"category\":\"%s\",\"priority\":\"%s\",\"dueDate\":\"%s\"," +
                        "\"completed\":%b,\"createdAt\":\"%s\",\"completedAt\":%s," +
                        "\"studentEmail\":\"%s\"}",
                id, escapeJson(title), escapeJson(description),
                escapeJson(category), priority.getValue(),
                dueDate.format(formatter), completed, createdAt.format(formatter),
                completedAt != null ? "\"" + completedAt.format(formatter) + "\"" : "null",
                escapeJson(studentEmail)
        );
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', category='%s', priority=%s, dueDate=%s, completed=%b}",
                id, title, category, priority, dueDate, completed);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}