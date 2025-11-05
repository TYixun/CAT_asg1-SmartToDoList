package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Category;
import com.example.demo.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional;

public class TodoController {

    // Task Input Fields
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<Priority> priorityComboBox;

    // Task Table
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, Category> categoryColumn;
    @FXML private TableColumn<Task, Priority> priorityColumn;
    @FXML private TableColumn<Task, Boolean> completedColumn;

    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> filterCategoryComboBox;
    @FXML private ComboBox<Priority> filterPriorityComboBox;
    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> filterStatusComboBox;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button markCompleteButton;
    @FXML private Button clearFiltersButton;

    // Service
    private TaskService taskService;
    private Task selectedTask;

    @FXML
    public void initialize() {
        taskService = new TaskService();

        // Setup ComboBoxes
        setupComboBoxes();

        // Setup Table
        setupTableView();

        // Load all tasks
        refreshTaskTable();

        // Setup listeners
        setupListeners();
    }

    private void setupComboBoxes() {
        // Category ComboBoxes
        categoryComboBox.getItems().setAll(Category.values());
        filterCategoryComboBox.getItems().add(null); // All categories
        filterCategoryComboBox.getItems().addAll(Category.values());

        // Priority ComboBoxes
        priorityComboBox.getItems().setAll(Priority.values());
        filterPriorityComboBox.getItems().add(null); // All priorities
        filterPriorityComboBox.getItems().addAll(Priority.values());

        // Status ComboBox
        filterStatusComboBox.getItems().addAll("All", "Completed", "Incomplete");
        filterStatusComboBox.setValue("All");
    }

    private void setupTableView() {
        // Set up column cell value factories
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        // Customize completed column
        completedColumn.setCellFactory(column -> new TableCell<Task, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✓" : "");
                }
            }
        });
    }

    private void setupListeners() {
        // Table selection listener
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedTask = newSelection;
                    updateButtonStates();
                }
        );

        // Search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Filter listeners
        filterCategoryComboBox.setOnAction(e -> applyFilters());
        filterPriorityComboBox.setOnAction(e -> applyFilters());
        filterDatePicker.setOnAction(e -> applyFilters());
        filterStatusComboBox.setOnAction(e -> applyFilters());
    }

    @FXML
    private void handleAddTask() {
        if (!validateInput()) {
            return;
        }

        Task task = new Task(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                dueDatePicker.getValue(),
                categoryComboBox.getValue(),
                priorityComboBox.getValue()
        );

        taskService.addTask(task);
        refreshTaskTable();
        clearInputFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Task added successfully!");
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to edit.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        Task updatedTask = new Task(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                dueDatePicker.getValue(),
                categoryComboBox.getValue(),
                priorityComboBox.getValue()
        );
        updatedTask.setCompleted(selectedTask.isCompleted());

        taskService.updateTask(selectedTask, updatedTask);
        refreshTaskTable();
        clearInputFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully!");
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Task");
        confirmAlert.setContentText("Are you sure you want to delete this task?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.deleteTask(selectedTask);
            refreshTaskTable();
            clearInputFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Task deleted successfully!");
        }
    }

    @FXML
    private void handleMarkComplete() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to mark.");
            return;
        }

        taskService.toggleTaskCompletion(selectedTask);
        refreshTaskTable();
    }

    @FXML
    private void handleLoadToForm() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to edit.");
            return;
        }

        titleField.setText(selectedTask.getTitle());
        descriptionArea.setText(selectedTask.getDescription());
        dueDatePicker.setValue(selectedTask.getDueDate());
        categoryComboBox.setValue(selectedTask.getCategory());
        priorityComboBox.setValue(selectedTask.getPriority());
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterCategoryComboBox.setValue(null);
        filterPriorityComboBox.setValue(null);
        filterDatePicker.setValue(null);
        filterStatusComboBox.setValue("All");
        refreshTaskTable();
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) taskTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Smart Todo List v1.0");
        alert.setContentText("A JavaFX Todo application for managing tasks.\n\n" +
                "Features:\n" +
                "- Add, Edit, Delete tasks\n" +
                "- Search and Filter\n" +
                "- Data persistence with JSON\n\n" +
                "Created for Software Development Workshop 2025/2026");
        alert.showAndWait();
    }

    private void applyFilters() {
        ObservableList<Task> filteredTasks = taskService.getAllTasks();

        // Search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            filteredTasks = taskService.searchTasks(searchText);
        }

        // Category filter
        Category selectedCategory = filterCategoryComboBox.getValue();
        if (selectedCategory != null && !filteredTasks.isEmpty()) {
            filteredTasks = filteredTasks.filtered(task -> task.getCategory() == selectedCategory);
        }

        // Priority filter
        Priority selectedPriority = filterPriorityComboBox.getValue();
        if (selectedPriority != null && !filteredTasks.isEmpty()) {
            filteredTasks = filteredTasks.filtered(task -> task.getPriority() == selectedPriority);
        }

        // Date filter
        LocalDate selectedDate = filterDatePicker.getValue();
        if (selectedDate != null && !filteredTasks.isEmpty()) {
            filteredTasks = filteredTasks.filtered(task ->
                    task.getDueDate() != null && task.getDueDate().equals(selectedDate));
        }

        // Status filter
        String status = filterStatusComboBox.getValue();
        if (status != null && !status.equals("All") && !filteredTasks.isEmpty()) {
            boolean completed = status.equals("Completed");
            filteredTasks = filteredTasks.filtered(task -> task.isCompleted() == completed);
        }

        taskTable.setItems(filteredTasks);
    }

    private void refreshTaskTable() {
        taskTable.setItems(taskService.getAllTasks());
    }

    private void clearInputFields() {
        titleField.clear();
        descriptionArea.clear();
        dueDatePicker.setValue(null);
        categoryComboBox.setValue(null);
        priorityComboBox.setValue(null);
        selectedTask = null;
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        markCompleteButton.setDisable(!hasSelection);
    }

    private boolean validateInput() {
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
}
