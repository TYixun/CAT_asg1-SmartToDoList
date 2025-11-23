package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Category;
import com.example.demo.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;

public class TodoController {

    // Task Input Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private TextField searchField; // Assume this exists for search
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, Category> categoryColumn;
    @FXML private TableColumn<Task, Priority> priorityColumn;
    @FXML private TableColumn<Task, Boolean> completedColumn;
    @FXML private Label statsLabel; // New: For "Completed: X / Total: Y"
    @FXML
    private void handleExit() {
        // Save tasks before exiting
        JsonUtil.saveTasks(taskService.getAllTasks());

        // Exit the application
        Platform.exit();
    }
    private TaskService taskService = new TaskService();

    @FXML
    private void initialize() {
        // Setup ComboBoxes
        categoryComboBox.getItems().addAll(Category.values());
        priorityComboBox.getItems().addAll(Priority.values());

        // Setup TableView columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        // Load tasks and update stats
        taskTable.setItems(taskService.getAllTasks());
        updateStats();

        // Unique: Color-code priority column
        priorityColumn.setCellFactory(column -> new TableCell<Task, Priority>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority.toString());
                    switch (priority) {
                        case URGENT -> getStyleClass().add("priority-urgent");
                        case HIGH -> getStyleClass().add("priority-high");
                        case MEDIUM -> getStyleClass().add("priority-medium");
                        case LOW -> getStyleClass().add("priority-low");
                    }
                }
            }
        });

        // Load tasks
        taskTable.setItems(taskService.getAllTasks());
        updateStats(); // New: Update stats on init
    }

    // Add task button handler
    @FXML
    private void addTask() {
        if (!validateInputs()) return;

        Task newTask = new Task(
                titleField.getText(),
                descriptionArea.getText(),
                dueDatePicker.getValue(),
                categoryComboBox.getValue(),
                priorityComboBox.getValue()
        );
        taskService.addTask(newTask);
        clearInputs();
        updateStats();
    }

    // Edit task (assume selected from table)
    @FXML
    private void editTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a task to edit.");
            return;
        }
        // Populate fields with selected, then save on confirm
        titleField.setText(selected.getTitle());
        // ... (similar for other fields)
        // After edit:
        taskService.editTask(selected, selected); // Update with new values
        updateStats();
    }

    // Delete task
    @FXML
    private void deleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Optional<ButtonType> result = showAlertWithConfirm("Confirm Delete", "Delete this task?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                taskService.deleteTask(selected);
                updateStats();
            }
        }
    }

    // Mark completed
    @FXML
    private void markCompleted() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setCompleted(!selected.isCompleted());
            taskService.markCompleted(selected, selected.isCompleted());
            taskTable.refresh();
            updateStats();
        }
    }

    // Search by keyword
    @FXML
    private void onSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            taskTable.setItems(taskService.getAllTasks());
            return;
        }
        ObservableList<Task> filtered = taskService.getAllTasks().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(keyword) ||
                        task.getDescription().toLowerCase().contains(keyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        taskTable.setItems(filtered);
    }

    // Filter handlers (e.g., from ComboBoxes or buttons)
    @FXML
    private void filterByCategory() {
        taskTable.setItems(taskService.filterByCategory(categoryComboBox.getValue()));
    }
    @FXML private void saveTasks() {
        JsonUtil.saveTasks(taskService.getAllTasks());
        showAlert(Alert.AlertType.INFORMATION, "Saved", "All tasks saved to JSON.");
    }

    @FXML private void showAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About",
                "Smart Todo List v1.0\nBuilt with JavaFX & SceneBuilder\n© 2025");
    }

    // Similar for other filters...

    // New: Update stats label
    private void updateStats() {
        long total = taskService.getAllTasks().size();
        long completed = taskService.getAllTasks().stream().filter(Task::isCompleted).count();
        statsLabel.setText("Completed: " + completed + " / Total: " + total);
    }

    // Validation (unchanged but here for completeness)
    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title is required!");
            return false;
        }
        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category is required!");
            return false;
        }
        if (priorityComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Priority is required!");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showAlertWithConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private void clearInputs() {
        titleField.clear();
        descriptionArea.clear();
        dueDatePicker.setValue(null);
        categoryComboBox.setValue(null);
        priorityComboBox.setValue(null);
    }
}