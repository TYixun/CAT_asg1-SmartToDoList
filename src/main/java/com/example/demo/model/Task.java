package com.example.demo.model;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String category;
    private Priority priority;
    private boolean completed;

    // Constructor
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.completed = false;
    }

    public Task(String title, String description, LocalDate dueDate,
            String category, Priority priority) {
        this();
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

    @Override
    public String toString() {
        return title + " - " + priority + " (" + category + ")";
    }
}
