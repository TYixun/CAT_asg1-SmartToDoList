package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Category;
import com.example.demo.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private ObservableList<Task> tasks;

    public TaskService() {
        tasks = FXCollections.observableArrayList();
        loadTasks();
    }

     //Add a new task
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    //Update an existing task
    public void updateTask(Task oldTask, Task newTask) {
        int index = tasks.indexOf(oldTask);
        if (index >= 0) {
            newTask.setId(oldTask.getId()); // Keep same ID
            tasks.set(index, newTask);
            saveTasks();
        }
    }

    /**
     * Delete a task
     */
    public void deleteTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    /**
     * Mark task as completed/uncompleted
     */
    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
        saveTasks();
    }

    /**
     * Get all tasks
     */
    public ObservableList<Task> getAllTasks() {
        return tasks;
    }

    /**
     * Search tasks by keyword (searches title and description)
     */
    public ObservableList<Task> searchTasks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return tasks;
        }

        String lowerKeyword = keyword.toLowerCase();
        return tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(lowerKeyword) ||
                        task.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filter tasks by category
     */
    public ObservableList<Task> filterByCategory(Category category) {
        if (category == null) {
            return tasks;
        }

        return tasks.stream()
                .filter(task -> task.getCategory().equals(category))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filter tasks by completion status
     */
    public ObservableList<Task> filterByStatus(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filter tasks by due date
     */
    public ObservableList<Task> filterByDueDate(LocalDate date) {
        if (date == null) {
            return tasks;
        }

        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Filter tasks by priority
     */
    public ObservableList<Task> filterByPriority(Priority priority) {
        if (priority == null) {
            return tasks;
        }

        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Save tasks to JSON file
     */
    private void saveTasks() {
        JsonUtil.saveTasks(tasks);
    }


    //Load tasks from JSON file
    private void loadTasks() {
        List<Task> loadedTasks = JsonUtil.loadTasks();
        tasks.addAll(loadedTasks);
    }
}
