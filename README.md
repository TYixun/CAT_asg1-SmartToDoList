# Smart Todo List

A desktop todo list application built with JavaFX for CAT201 assignment.

## About

This is our group project for CAT201 Integrated Software Development Workshop. We built a todo list app that helps users manage their tasks with features like categories, priorities, and filters.

## Features

- Add, edit and delete tasks
- Set due dates for tasks
- Assign priority levels (Urgent, High, Medium, Low)
- Organize tasks into categories (Work, Personal, School, etc.)
- Filter tasks by category, priority, date or completion status
- Search function to find tasks quickly
- Sort tasks by priority, date or default order
- Tasks are saved automatically in JSON format

## How to Run

1. Make sure you have JDK 25 installed
2. Clone this repository
3. Open the project in IntelliJ IDEA or your preferred IDE
4. Run the Main.java file

Or if you're using Maven:
```
mvn javafx:run
```

## Technologies

- Java 25
- JavaFX 21.0.6
- Maven for build management
- Gson library for JSON handling

## Project Structure

The main code is in `src/main/java/com/example/demo/`:
- `Main.java` - starts the application
- `TodoController.java` - handles all the UI logic and user interactions
- `TaskService.java` - manages task data and operations
- `Task.java` - the task model
- `Priority.java` - priority levels enum

The UI is defined in `MainView.fxml` and styled with `styles.css`.

## Group Members

- Teoh Yixun
- Alvin Teh Jun You
- Tan Chuan Huan

## Notes

We used JavaFX for the GUI because it provides good desktop application support. The app saves all tasks to a JSON file so your data persists between sessions.

---

CAT201 Integrated Software Development Workshop 2025/2026
