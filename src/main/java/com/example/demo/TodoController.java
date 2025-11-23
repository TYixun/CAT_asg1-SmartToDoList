package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TodoController {

    // --- Constants ---
    private static final String STYLE_NAV_BUTTON = "nav-button";
    private static final String STYLE_SELECTED = "selected";
    private static final String STYLE_ACTION_BUTTON = "action-button";
    private static final String STYLE_SECONDARY_BUTTON = "secondary-button";

    private static final String PRIORITY_HIGH_STYLE = "priority-high";
    private static final String PRIORITY_MEDIUM_STYLE = "priority-medium";
    private static final String PRIORITY_LOW_STYLE = "priority-low";
    private static final String PRIORITY_URGENT_STYLE = "priority-urgent";

    // Task Input Fields (Inspector)
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<Priority> priorityComboBox;

    // Task Table
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Task, String> categoryColumn;
    @FXML
    private TableColumn<Task, Priority> priorityColumn;
    @FXML
    private TableColumn<Task, Boolean> completedColumn;

    // Search and Filter
    @FXML
    private TextField searchField;
    @FXML
    private VBox categoryListVBox;
    @FXML
    private DatePicker sidebarDatePicker;

    // Buttons
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    // Sidebar Buttons (for highlighting)
    private Button activeNavButton;

    // Service
    private TaskService taskService;
    private Task selectedTask;

    // State
    private boolean isEditMode = false;

    // Current Filter State
    private enum FilterMode {
        ALL, TODAY, IMPORTANT, COMPLETED, CATEGORY, DATE
    }

    private FilterMode currentFilterMode = FilterMode.ALL;
    private String currentCategoryFilter = null;
    private LocalDate currentDateFilter = null;

    // Default Categories
    private static final Set<String> DEFAULT_CATEGORIES = new HashSet<>(Arrays.asList(
            "WORK", "PERSONAL", "SHOPPING", "HEALTH", "FINANCE", "SCHOOL"));

    @FXML
    public void initialize() {
        taskService = new TaskService();
        setupComboBoxes();
        setupTableView();
        setupCategoryList();
        refreshTaskTable();
        setupListeners();
        clearInputFields();
    }

    private void setupComboBoxes() {
        updateCategoryComboBox();
        priorityComboBox.getItems().setAll(Priority.values());
    }

    private void updateCategoryComboBox() {
        Set<String> categories = new HashSet<>(DEFAULT_CATEGORIES);
        categories.addAll(taskService.getUniqueCategories());
        categoryComboBox.getItems().setAll(categories);
    }

    private void setupTableView() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        // Custom cell for completion checkbox
        completedColumn.setCellFactory(column -> new TableCell<Task, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    taskService.toggleTaskCompletion(task);
                    refreshTaskTable();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });

        // Custom cell for Priority Color Coding
        priorityColumn.setCellFactory(column -> new TableCell<Task, Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                getStyleClass().removeAll(PRIORITY_HIGH_STYLE, PRIORITY_MEDIUM_STYLE, PRIORITY_LOW_STYLE,
                        PRIORITY_URGENT_STYLE);

                if (!empty && item != null) {
                    setText(item.toString());
                    switch (item) {
                        case HIGH -> getStyleClass().add(PRIORITY_HIGH_STYLE);
                        case MEDIUM -> getStyleClass().add(PRIORITY_MEDIUM_STYLE);
                        case LOW -> getStyleClass().add(PRIORITY_LOW_STYLE);
                        case URGENT -> getStyleClass().add(PRIORITY_URGENT_STYLE);
                    }
                }
            }
        });
    }

    private void setupCategoryList() {
        categoryListVBox.getChildren().clear();
        Set<String> categories = taskService.getUniqueCategories();

        for (String category : categories) {
            Button catBtn = new Button(category);
            catBtn.setMaxWidth(Double.MAX_VALUE);
            catBtn.getStyleClass().add(STYLE_NAV_BUTTON);
            catBtn.setOnAction(e -> {
                filterByCategory(category);
                setActiveNavButton(catBtn);
            });
            categoryListVBox.getChildren().add(catBtn);
        }
    }

    private void setupListeners() {
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedTask = newSelection;
                        isEditMode = false;
                        loadTaskToForm(selectedTask);
                        updateButtonStates();
                    }
                });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        sidebarDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterByDate(newVal);
                setActiveNavButton(null);
            }
        });
    }

    private void setActiveNavButton(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove(STYLE_SELECTED);
        }
        activeNavButton = btn;
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().add(STYLE_SELECTED);
        }
    }

    // --- Navigation Filters ---

    @FXML
    private void filterAll(ActionEvent event) {
        setFilterMode(FilterMode.ALL, event);
    }

    @FXML
    private void filterToday(ActionEvent event) {
        setFilterMode(FilterMode.TODAY, event);
    }

    @FXML
    private void filterImportant(ActionEvent event) {
        setFilterMode(FilterMode.IMPORTANT, event);
    }

    @FXML
    private void filterCompleted(ActionEvent event) {
        setFilterMode(FilterMode.COMPLETED, event);
    }

    private void setFilterMode(FilterMode mode, ActionEvent event) {
        currentFilterMode = mode;
        sidebarDatePicker.setValue(null);
        applyFilters();
        if (event != null && event.getSource() instanceof Button) {
            setActiveNavButton((Button) event.getSource());
        }
    }

    private void filterByCategory(String category) {
        currentFilterMode = FilterMode.CATEGORY;
        currentCategoryFilter = category;
        sidebarDatePicker.setValue(null);
        applyFilters();
    }

    private void filterByDate(LocalDate date) {
        currentFilterMode = FilterMode.DATE;
        currentDateFilter = date;
        applyFilters();
    }

    private void applyFilters() {
        ObservableList<Task> filteredTasks = taskService.getAllTasks();

        switch (currentFilterMode) {
            case TODAY -> filteredTasks = taskService.filterByDueDate(LocalDate.now());
            case IMPORTANT -> filteredTasks = taskService.filterByPriority(Priority.HIGH);
            case COMPLETED -> filteredTasks = taskService.filterByStatus(true);
            case CATEGORY -> {
                if (currentCategoryFilter != null)
                    filteredTasks = taskService.filterByCategory(currentCategoryFilter);
            }
            case DATE -> {
                if (currentDateFilter != null)
                    filteredTasks = taskService.filterByDueDate(currentDateFilter);
            }
        }

        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerKeyword = searchText.toLowerCase();
            filteredTasks = filteredTasks.filtered(task -> task.getTitle().toLowerCase().contains(lowerKeyword) ||
                    task.getDescription().toLowerCase().contains(lowerKeyword));
        }

        taskTable.setItems(filteredTasks);
    }

    // --- CRUD Operations ---

    @FXML
    private void handleAddTask() {
        if (selectedTask == null) {
            if (!validateInput())
                return;

            Task task = createTaskFromInput();
            if (task == null)
                return; // Validation failed in helper

            taskService.addTask(task);
            refreshUIAfterUpdate();
            clearInputFields();
        } else {
            clearInputFields();
        }
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null)
            return;

        if (!isEditMode) {
            isEditMode = true;
            updateButtonStates();
        } else {
            if (!validateInput())
                return;

            Task updatedTask = createTaskFromInput();
            if (updatedTask == null)
                return;

            updatedTask.setCompleted(selectedTask.isCompleted());
            taskService.updateTask(selectedTask, updatedTask);

            refreshUIAfterUpdate();

            isEditMode = false;
            updateButtonStates();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully!");
        }
    }

    private Task createTaskFromInput() {
        String category = categoryComboBox.getValue();
        if (category == null || category.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category is required!");
            return null;
        }

        return new Task(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                dueDatePicker.getValue(),
                category.trim().toUpperCase(),
                priorityComboBox.getValue());
    }

    private void refreshUIAfterUpdate() {
        setupCategoryList();
        updateCategoryComboBox();
        refreshTaskTable();
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null)
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Task");
        confirmAlert.setContentText("Are you sure you want to delete this task?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.deleteTask(selectedTask);
            refreshTaskTable();
            clearInputFields();
        }
    }

    private void loadTaskToForm(Task task) {
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        categoryComboBox.setValue(task.getCategory());
        priorityComboBox.setValue(task.getPriority());
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove(STYLE_SELECTED);
            activeNavButton = null;
        }
        setFilterMode(FilterMode.ALL, null);
    }

    @FXML
    private void handleExit() {
        ((Stage) taskTable.getScene().getWindow()).close();
    }

    @FXML
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About",
                "Smart Todo List v2.0\n\n" +
                        "Created for CAT201 Integrated Software Development Workshop 2025/2026\n" +
                        "By Teoh Yixun, Alvin Teh Jun You, Tan Chuan Huan");
    }

    private void refreshTaskTable() {
        applyFilters();
    }

    private void clearInputFields() {
        titleField.clear();
        descriptionArea.clear();
        dueDatePicker.setValue(null);
        categoryComboBox.setValue(null);
        priorityComboBox.setValue(null);

        selectedTask = null;
        isEditMode = false;
        taskTable.getSelectionModel().clearSelection();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        boolean enableInputs = !hasSelection || isEditMode;

        setInputsDisabled(!enableInputs);

        if (hasSelection) {
            configureButton(addButton, true, "New Task", STYLE_SECONDARY_BUTTON);
            configureButton(editButton, true, isEditMode ? "Save Changes" : "Edit Task",
                    isEditMode ? STYLE_ACTION_BUTTON : STYLE_SECONDARY_BUTTON);
            deleteButton.setDisable(false);
        } else {
            configureButton(addButton, true, "Add Task", STYLE_ACTION_BUTTON);
            configureButton(editButton, false, "Update Task", STYLE_ACTION_BUTTON); // Hidden
            deleteButton.setDisable(true);
        }
    }

    private void setInputsDisabled(boolean disabled) {
        titleField.setDisable(disabled);
        descriptionArea.setDisable(disabled);
        dueDatePicker.setDisable(disabled);
        categoryComboBox.setDisable(disabled);
        priorityComboBox.setDisable(disabled);
    }

    private void configureButton(Button btn, boolean visible, String text, String styleClass) {
        btn.setVisible(visible);
        btn.setManaged(visible);
        btn.setText(text);
        btn.getStyleClass().removeAll(STYLE_ACTION_BUTTON, STYLE_SECONDARY_BUTTON);
        btn.getStyleClass().add(styleClass);
        btn.setDisable(false);
    }

    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title is required!");
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
