package com.example.demo;

import com.example.demo.model.Priority;                 // The enum defining task priority levels
import com.example.demo.model.Task;                     // The data model class for individual tasks
import javafx.fxml.FXML;                                // The FXML file
import javafx.scene.control.*;                          // Contains most UI controls (Button, TableView, ComboBox, etc.)
import javafx.scene.control.cell.PropertyValueFactory;  // Used to map Task properties to TableColumn cells
import javafx.collections.ObservableList;               // JavaFX list that automatically updates bound UI components
import javafx.collections.FXCollections;                // Utility class for creating ObservableLists
import javafx.stage.Stage;                              // The window container
import javafx.event.ActionEvent;                        // Represents an event triggered by a UI action (e.g., button click)
import java.time.LocalDate;                             // Used for handling due dates and date filtering
import java.util.Arrays;                                // Utility class for array operations
import java.util.HashSet;                               // Used for managing the set of unique categories efficiently
import java.util.Optional;                              // Used for handling the result of confirmation dialogs (Alerts)
import java.util.Set;                                   // Interface for collections of unique elements

public class TodoController {

    // Constants
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
    public void initialize() { // Initializes the TaskService, sets up UI controls, listeners, and the table view
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
        updateCategoryComboBox(); // input priority ComboBox with all enum values
        priorityComboBox.getItems().setAll(Priority.values());
    }

    private void setupViewByComboBox() {
        viewByComboBox.getItems().addAll("Default", "Priority", "Date"); // Options for sorting/viewing the task list
        viewByComboBox.setValue("Default");
        viewByComboBox.setOnAction(e -> applyViewBySort()); // Apply the selected sort whenever the value changes
    }

    private void setupPriorityFilter() {
        priorityFilterComboBox.getItems().add(null); // "All" option
        priorityFilterComboBox.getItems().addAll(Priority.values());
        priorityFilterComboBox.setPromptText("All Priorities");
        priorityFilterComboBox.setOnAction(e -> applyFilters());
    }

    private void updateCategoryComboBox() {
        Set<String> categories = new HashSet<>(DEFAULT_CATEGORIES); // input ComboBox with default categories plus unique categories from existing tasks
        categories.addAll(taskService.getUniqueCategories());
        categoryComboBox.getItems().setAll(categories);
    }

    private void setupTableView() {
        // Map Task object properties to table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        // Custom cell for completion checkbox
        completedColumn.setCellFactory(column -> new TableCell<Task, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                // Action listener to toggle completion status when the checkbox is clicked
                checkBox.setOnAction(event -> {
                    // Get the Task associated with this row/cell
                    Task task = getTableView().getItems().get(getIndex());
                    taskService.toggleTaskCompletion(task);
                    refreshTaskTable(); // ensure sorting/filtering reflects changes
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { // If the cell has no content, remove any graphic
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox); // Set the CheckBox as the cell's content
                }
            }
        });

        // Custom cell for Priority Color Coding
        priorityColumn.setCellFactory(column -> new TableCell<Task, Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                // Remove all priority styles before adding the correct one
                getStyleClass().removeAll(PRIORITY_HIGH_STYLE, PRIORITY_MEDIUM_STYLE, PRIORITY_LOW_STYLE,
                        PRIORITY_URGENT_STYLE);

                if (!empty && item != null) {
                    setText(item.toString());
                    // Add the corresponding CSS class based on the Priority value
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
        updateCategoryFilter(); // initialize the filter ComboBox
        categoryFilterComboBox.setOnAction(e -> {
            String selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                filterByCategory(selectedCategory); // Apply category filter
            } else {
                setFilterMode(FilterMode.ALL, null); // Revert to All tasks
                updateFilterLabel("All Tasks");
            }
        });
    }

    private void updateCategoryFilter() {
        // initialize the filter ComboBox with existing unique categories plus the "All" option
        Set<String> categories = taskService.getUniqueCategories();
        ObservableList<String> categoryList = FXCollections.observableArrayList("All Categories");
        categoryList.addAll(categories);
        categoryFilterComboBox.setItems(categoryList);
        categoryFilterComboBox.setValue("All Categories");
    }

    private void setupListeners() {
        // Listener for task selection in the table
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedTask = newSelection;
                        isEditMode = false; // Always exit edit mode on new selection
                        loadTaskToForm(selectedTask);
                        updateButtonStates(); // Update Add/Edit/Delete buttons
                    }
                });

        // Listener for the search field text change (applies filter dynamically)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Listener for the sidebar date picker (applies date filter)
        sidebarDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterByDate(newVal);
                setActiveNavButton(null); // Clear selection on other navigation buttons
            }
        });
    }

    // Manages the styling for the sidebar navigation buttons.
    private void setActiveNavButton(Button btn) {
        if (activeNavButton != null) { // Remove highlight from previously active button
            activeNavButton.getStyleClass().remove(STYLE_SELECTED);
        }
        activeNavButton = btn; // Update the reference to the new active button
        if (activeNavButton != null) { // Add highlight style to the new active button
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

    // Sets the current filter mode, clears the date picker, and applies the filter
    private void setFilterMode(FilterMode mode, ActionEvent event) {
        currentFilterMode = mode;
        sidebarDatePicker.setValue(null); // Clear date picker when using general filters
        applyFilters();
        // If the event came from a button, mark it as the active navigation button
        if (event != null && event.getSource() instanceof Button) {
            setActiveNavButton((Button) event.getSource());
        }
    }

    // category filter mode
    private void filterByCategory(String category) {
        currentFilterMode = FilterMode.CATEGORY;
        currentCategoryFilter = category;
        sidebarDatePicker.setValue(null); // Clear date picker
        updateFilterLabel(category); // Update the UI to show the active filter
        applyFilters(); // Apply the updated filters
    }

    // date filter mode
    private void filterByDate(LocalDate date) {
        currentFilterMode = FilterMode.DATE;
        currentDateFilter = date;
        updateFilterLabel(date.toString()); // Update the UI to show the selected date
        applyFilters(); // Apply the updated filters
    }

    // Updates the UI label to display the active filter name
    private void updateFilterLabel(String filterName) {
        filterLabel.setText("- " + filterName);
    }

    // Applies the selected sorting order to the currently displayed tasks
    private void applyViewBySort() {
        String sortBy = viewByComboBox.getValue();
        ObservableList<Task> tasks = taskTable.getItems();

        if ("Priority".equals(sortBy)) {
            // Sort by Priority level (highest level first)
            tasks.sort((t1, t2) -> {
                Priority p1 = t1.getPriority();
                Priority p2 = t2.getPriority();
                if (p1 == null && p2 == null)
                    return 0;
                if (p1 == null)
                    return 1;
                if (p2 == null)
                    return -1;
                // Compares numerical levels: URGENT (4) > HIGH (3) > MEDIUM (2) > LOW (1)
                return Integer.compare(p2.getLevel(), p1.getLevel());
            });
        } else if ("Date".equals(sortBy)) {
            // Sort by Due Date (earliest date first)
            tasks.sort((t1, t2) -> {
                LocalDate d1 = t1.getDueDate();
                LocalDate d2 = t2.getDueDate();
                if (d1 == null && d2 == null)
                    return 0;
                if (d1 == null) // Null due date goes last
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
                // t2 vs t1 compares newest first (descending order)
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            });
        }
    }

    // Applies the current filter state (sidebar, search, priority) to the task list and updates the TableView.
    private void applyFilters() {
        ObservableList<Task> filteredTasks = taskService.getAllTasks(); // Start with the full list from the service

        // Apply primary sidebar/navigation filter
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

        // Apply search filter
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
    // Handles the logic for adding a new task
    private void handleAddTask() {
        if (selectedTask == null) { // No task selected → user is adding a new task
            if (!validateInput()) // Validate the form input; stop if invalid
                return;

            Task task = createTaskFromInput(); // Create a Task from the input fields
            if (task == null)
                return; // Validation failed in helper

            taskService.addTask(task);
            refreshUIAfterUpdate();
            clearInputFields(); // Reset input fields for the next entry
        } else {
            clearInputFields(); // A task is selected → user cancels editing
        }
    }

    @FXML
    // Handles the logic for editing a new task
    private void handleEditTask() {
        if (selectedTask == null) // No task selected → nothing to edit
            return;

        if (!isEditMode) { // Enter Edit Mode
            isEditMode = true;
            updateButtonStates(); // Update UI (buttons, form state)
        } else {
            if (!validateInput())
                return;

            Task updatedTask = createTaskFromInput(); // Create updated task from user input
            if (updatedTask == null)
                return;

            updatedTask.setCompleted(selectedTask.isCompleted()); // Preserve the "completed" state
            taskService.updateTask(selectedTask, updatedTask); // Apply updates to the task service

            refreshUIAfterUpdate(); // Refresh UI and exit edit mode
            isEditMode = false;
            updateButtonStates();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully!");
        }
    }

    // Creates a new Task object from the current input field values
    private Task createTaskFromInput() {
        // Validate category is not empty
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

    // Refresh UI
    private void refreshUIAfterUpdate() {
        updateCategoryComboBox(); // Update ComboBoxes in case a new category was created
        updateCategoryFilter();
        refreshTaskTable(); // Refresh the table display
    }

    @FXML
    // Handles the logic for deleting a task
    private void handleDeleteTask() {
        if (selectedTask == null)
            return;

        // Show confirmation dialog before deleting
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Task");
        confirmAlert.setContentText("Are you sure you want to delete this task?");

        // Wait for user confirmation
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskService.deleteTask(selectedTask);
            refreshTaskTable();
            clearInputFields();
        }
    }

    // Loads the details of the selected task into the input form fields
    private void loadTaskToForm(Task task) {
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        categoryComboBox.setValue(task.getCategory());
        priorityComboBox.setValue(task.getPriority());
    }

    // --- General UI Handlers ---

    @FXML
    // Handles the logic for clearing filter
    private void handleClearFilters() {
        // Reset all filtering/sorting controls to default state
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
    // Closes the application window
    private void handleExit() {
        ((Stage) taskTable.getScene().getWindow()).close();
    }

    @FXML
    // Display message about this application
    private void handleAbout() {
        showAlert(Alert.AlertType.INFORMATION, "About",
                "Smart Todo List v2.0\n\n" +
                        "Created for CAT201 Integrated Software Development Workshop 2025/2026\n" +
                        "By Teoh Yixun, Alvin Teh Jun You, Tan Chuan Huan");
    }

    private void refreshTaskTable() {
        applyFilters();
    }

    // Resets all input fields, clears the selected task, and updates button visibility
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

    // Manages the visibility, text, and disabled state of the input fields and CRUD buttons
    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        boolean enableInputs = !hasSelection || isEditMode;

        setInputsDisabled(!enableInputs); // Disable inputs if no selection AND not in edit mode

        if (hasSelection) {
            // Task is selected
            configureButton(addButton, true, "New Task", STYLE_SECONDARY_BUTTON);
            configureButton(editButton, true, isEditMode ? "Save Changes" : "Edit Task",
                    isEditMode ? STYLE_ACTION_BUTTON : STYLE_SECONDARY_BUTTON);
            deleteButton.setDisable(false);
        } else {
            // No task is selected (default 'Add' mode)
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

    // configure the appearance and visibility of a button
    private void configureButton(Button btn, boolean visible, String text, String styleClass) {
        btn.setVisible(visible);
        btn.setManaged(visible);
        btn.setText(text);
        btn.getStyleClass().removeAll(STYLE_ACTION_BUTTON, STYLE_SECONDARY_BUTTON);
        btn.getStyleClass().add(styleClass);
        btn.setDisable(false);
    }

    // Input validation
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

    // Display a simple alert dialog
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
