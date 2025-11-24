package com.example.demo.model;

import java.time.LocalDate;     // Used for handling dates (like dueDate)
import java.time.LocalDateTime; // Used for handling dates and times (like createdAt)
import java.util.UUID;          // Used for generating a unique identifier (UUID) for the task ID

public class Task {
    // --- Fields (Task Attributes) ---
    private String id;                  // Unique identifier, automatically generated.
    private String title;               // The title of the task.
    private String description;         // Detailed explanation of the task
    private LocalDate dueDate;          // The date when the task is expected to be finished
    private String category;            // A grouping label (e.g., "Work," "Personal," "Groceries")
    private Priority priority;          // The urgency level (e.g., LOW, HIGH, URGENT)
    private boolean completed;          // Status flag: true if done, false otherwise
    private LocalDateTime createdAt;    // Timestamp of when the task was created

    // Default Constructor
    public Task() {
        this.id = UUID.randomUUID().toString(); // Automatically generate a unique ID
        this.completed = false; // Default state is not completed
        this.createdAt = LocalDateTime.now(); // Record the creation time
    }

    //Parameterized constructor for creating a new task with all required details.
    public Task(String title, String description, LocalDate dueDate,
            String category, Priority priority) {
        this(); // Calls the default constructor to initialize id, completed, and createdAt
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // @return A formatted string containing the title, priority, and category
    @Override
    public String toString() {
        return title + " - " + priority + " (" + category + ")";
    }
}
