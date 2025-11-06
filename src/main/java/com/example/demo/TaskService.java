package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Category;
import com.example.demo.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private ObservableList<Task> tasks;

    public TaskService() {
        // Create ObservableList and load tasks
        tasks = FXCollections.observableArrayList();
        loadTasks();
    }

    // Add a new task and auto-save
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks(); // Auto-save
    }

    // Edit task and auto-save
    public void editTask(Task oldTask, Task newTask) {
        int index = tasks.indexOf(oldTask);
        if (index != -1) {
            tasks.set(index, newTask);
            saveTasks(); // Auto-save
        }
    }

    // Delete task and auto-save
    public void deleteTask(Task task) {
        tasks.remove(task);
        saveTasks(); // Auto-save
    }

    // Mark as completed and auto-save
    public void markCompleted(Task task, boolean completed) {
        task.setCompleted(completed);
        saveTasks(); // Auto-save
    }

    public ObservableList<Task> getAllTasks() {
        return tasks;
    }

    // Simple unique enhancement: Sort by priority (descending) or due date
    public void sortByPriority() {
        tasks.sort(Comparator.comparingInt(task -> -task.getPriority().getLevel())); // Descending
    }

    public void sortByDueDate() {
        tasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    // Filter by category (using lambda)
    public ObservableList<Task> filterByCategory(Category category) {
        if (category == null) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> task.getCategory() == category)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Filter by due date
    public ObservableList<Task> filterByDueDate(LocalDate date) {
        if (date == null) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Filter by priority
    public ObservableList<Task> filterByPriority(Priority priority) {
        if (priority == null) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Filter by completion status
    public ObservableList<Task> filterByCompletion(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Save tasks to JSON file
    private void saveTasks() {
        JsonUtil.saveTasks(tasks);
    }

    // Load tasks from JSON file
    private void loadTasks() {
        List<Task> loadedTasks = JsonUtil.loadTasks();
        tasks.addAll(loadedTasks);
    }
}