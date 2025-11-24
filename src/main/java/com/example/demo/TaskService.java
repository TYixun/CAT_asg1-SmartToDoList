package com.example.demo;

import com.example.demo.model.Priority;     // Import the Priority enum for filtering tasks
import com.example.demo.model.Task;         // Import the Task model class
import javafx.collections.FXCollections;    // Utility class for creating JavaFX observable collections
import javafx.collections.ObservableList;   // JavaFX list interface that notifies listeners of changes (key for UI binding)
import java.time.LocalDate;                 // Used for filtering tasks by their due date
import java.util.List;                      // Standard Java list interface
import java.util.Set;                       // Used to return a collection of unique categories
import java.util.stream.Collectors;         // Used for collecting results from Stream API operations

// manage the collection of Task objects
public class TaskService {
    private ObservableList<Task> tasks; // can automatically update whenever the list contents change

    // Constructor
    public TaskService() {
        // Create ObservableList and load tasks
        tasks = FXCollections.observableArrayList();
        loadTasks();
    }

    // Add a new task
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    // Update an existing task
    public void updateTask(Task oldTask, Task newTask) {
        int index = tasks.indexOf(oldTask);
        if (index >= 0) {
            newTask.setId(oldTask.getId()); // ensure the updated task keeps the original unique ID
            tasks.set(index, newTask);      // Replace the old object at the found index
            saveTasks();
        }
    }

    // Delete a task
    public void deleteTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    // Mark task as completed/uncompleted
    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted()); // Inverts the current completed status
        saveTasks();
    }

    // Get all tasks
    public ObservableList<Task> getAllTasks() {
        return tasks;
    }

    // Get unique categories from existing tasks
    public Set<String> getUniqueCategories() {
        return tasks.stream()
                .map(Task::getCategory) // Get the category of each task
                .filter(c -> c != null && !c.trim().isEmpty()) // Filter out nulls or empty strings
                .collect(Collectors.toSet());   // Collect the results into a Set (automatically handles uniqueness)
    }

    // Search tasks by keyword (searches title and description)
    public ObservableList<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return tasks; // Return the full list if no keyword is provided
        }

        String lowerKeyword = keyword.toLowerCase();
        return tasks.stream() // check title and description
                .filter(task -> task.getTitle().toLowerCase().contains(lowerKeyword) ||
                        task.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Collect into a new ObservableList
    }

    // Filter tasks by category
    public ObservableList<Task> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return tasks; // Return the full list if no category is provided
        }

        return tasks.stream()
                .filter(task -> category.equals(task.getCategory()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Collect into a new ObservableList
    }

    // Filter tasks by completion status
    public ObservableList<Task> filterByStatus(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Collect into a new ObservableList
    }

    // Filter tasks by due date
    public ObservableList<Task> filterByDueDate(LocalDate date) {
        if (date == null) {
            return tasks; // Return the full list if no date is provided
        }

        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Collect into a new ObservableList
    }

    // Filter tasks by priority
    public ObservableList<Task> filterByPriority(Priority priority) {
        if (priority == null) {
            return tasks; // Return the full list if no priority is provided
        }

        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Collect into a new ObservableList
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
