package com.example.demo;

import com.example.demo.model.Priority;
import com.example.demo.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TodoController {

    // --- Constants ---
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
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private DatePicker sidebarDatePicker;
    @FXML
    private Label filterLabel;
    @FXML
    private ComboBox<String> viewByComboBox;
    @FXML
    private ComboBox<Priority> priorityFilterComboBox;

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
        ALL, TODAY, COMPLETED, INCOMPLETE, CATEGORY, DATE
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
        setupViewByComboBox();
        setupPriorityFilter();
        setupCategoryFilter();
        setupTableView();
        refreshTaskTable();
        setupListeners();
        clearInputFields();
    }

    private void setupComboBoxes() {
        updateCategoryComboBox();
        priorityComboBox.getItems().setAll(Priority.values());
    }

    private void setupViewByComboBox() {
        viewByComboBox.getItems().addAll("Default", "Priority", "Date");
        viewByComboBox.setValue("Default");
        viewByComboBox.setOnAction(e -> applyViewBySort());
    }

    private void setupPriorityFilter() {
        priorityFilterComboBox.getItems().add(null); // "All" option
        priorityFilterComboBox.getItems().addAll(Priority.values());
        priorityFilterComboBox.setPromptText("All Priorities");
        priorityFilterComboBox.setOnAction(e -> applyFilters());
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

    private void setupCategoryFilter() {
        updateCategoryFilter();
        categoryFilterComboBox.setOnAction(e -> {
            String selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                filterByCategory(selectedCategory);
            } else {
                setFilterMode(FilterMode.ALL, null);
                updateFilterLabel("All Tasks");
            }
        });
    }

    private void updateCategoryFilter() {
        Set<String> categories = taskService.getUniqueCategories();
        ObservableList<String> categoryList = FXCollections.observableArrayList("All Categories");
        categoryList.addAll(categories);
        categoryFilterComboBox.setItems(categoryList);
        categoryFilterComboBox.setValue("All Categories");
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
        updateFilterLabel("All Tasks");
    }

    @FXML
    private void filterToday(ActionEvent event) {
        setFilterMode(FilterMode.TODAY, event);
        updateFilterLabel("Today");
    }

    @FXML
    private void filterCompleted(ActionEvent event) {
        setFilterMode(FilterMode.COMPLETED, event);
        updateFilterLabel("Completed");
    }

    @FXML
    private void filterIncomplete(ActionEvent event) {
        setFilterMode(FilterMode.INCOMPLETE, event);
        updateFilterLabel("Incomplete");
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
        updateFilterLabel(category);
        applyFilters();
    }

    private void filterByDate(LocalDate date) {
        currentFilterMode = FilterMode.DATE;
        currentDateFilter = date;
        updateFilterLabel(date.toString());
        applyFilters();
    }

    private void updateFilterLabel(String filterName) {
        filterLabel.setText("- " + filterName);
    }

    private void applyViewBySort() {
        String sortBy = viewByComboBox.getValue();
        ObservableList<Task> tasks = taskTable.getItems();

        if ("Priority".equals(sortBy)) {
            tasks.sort((t1, t2) -> {
                Priority p1 = t1.getPriority();
                Priority p2 = t2.getPriority();
                if (p1 == null && p2 == null)
                    return 0;
                if (p1 == null)
                    return 1;
                if (p2 == null)
                    return -1;
                // URGENT (4) > HIGH (3) > MEDIUM (2) > LOW (1)
                return Integer.compare(p2.getLevel(), p1.getLevel());
            });
        } else if ("Date".equals(sortBy)) {
            tasks.sort((t1, t2) -> {
                LocalDate d1 = t1.getDueDate();
                LocalDate d2 = t2.getDueDate();
                if (d1 == null && d2 == null)
                    return 0;
                if (d1 == null)
                    return 1;
                if (d2 == null)
                    return -1;
                return d1.compareTo(d2);
            });
        } else {
            // Default: sort by creation time (newest first)
            tasks.sort((t1, t2) -> {
                if (t1.getCreatedAt() == null && t2.getCreatedAt() == null)
                    return 0;
                if (t1.getCreatedAt() == null)
                    return 1;
                if (t2.getCreatedAt() == null)
                    return -1;
                return t2.getCreatedAt().compareTo(t1.getCreatedAt()); // Newest first
            });
        }
    }

    private void applyFilters() {
        ObservableList<Task> filteredTasks = taskService.getAllTasks();

        switch (currentFilterMode) {
            case TODAY -> filteredTasks = taskService.filterByDueDate(LocalDate.now());
            case COMPLETED -> filteredTasks = taskService.filterByStatus(true);
            case INCOMPLETE -> filteredTasks = taskService.filterByStatus(false);
            case CATEGORY -> {
                if (currentCategoryFilter != null)
                    filteredTasks = taskService.filterByCategory(currentCategoryFilter);
            }
            case DATE -> {
                if (currentDateFilter != null)
                    filteredTasks = taskService.filterByDueDate(currentDateFilter);
            }
        }

        // Apply priority filter
        Priority selectedPriority = priorityFilterComboBox.getValue();
        if (selectedPriority != null) {
            filteredTasks = filteredTasks.filtered(task -> task.getPriority() == selectedPriority);
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

        // Validate due date is not before today
        LocalDate dueDate = dueDatePicker.getValue();
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Due date cannot be before today!");
            return null;
        }

        return new Task(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                dueDate,
                category.trim().toUpperCase(),
                priorityComboBox.getValue());
    }

    private void refreshUIAfterUpdate() {
        updateCategoryComboBox();
        updateCategoryFilter();
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
        viewByComboBox.setValue("Default");
        priorityFilterComboBox.setValue(null);
        categoryFilterComboBox.setValue("All Categories");
        sidebarDatePicker.setValue(null);
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove(STYLE_SELECTED);
            activeNavButton = null;
        }
        setFilterMode(FilterMode.ALL, null);
        updateFilterLabel("All Tasks");
    }

    @FXML
    private void handleClearForm() {
        clearInputFields();
        taskTable.getSelectionModel().clearSelection();
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
