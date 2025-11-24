package com.example.demo;

import javafx.application.Application; // Core class for any JavaFX application, required to extend
import javafx.fxml.FXMLLoader;         // Used to load and parse the FXML file (the layout/view definition)
import javafx.scene.Parent;            // Abstract base class for all nodes that have children, typically the root of a scene graph
import javafx.scene.Scene;             // The container for all content in a JavaFX scene graph
import javafx.stage.Stage;             // The top-level container for a JavaFX application, equivalent to a window

/*
    The main entry point for the Smart Todo List JavaFX application.
    This class handles the initialization of the application window (Stage)
    and loads the main user interface layout from an FXML file.
*/
public class Main extends Application {

    // The primary entry method for all JavaFX applications
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 1200, 750);

            // Setup stage
            primaryStage.setTitle("Smart Todo List"); // Sets the text displayed in the window's title bar
            primaryStage.setScene(scene);             // Places the Scene onto the Stage
            primaryStage.setMinWidth(1000);           // Defines the minimum allowed width for the window
            primaryStage.setMinHeight(750);           // Defines the minimum allowed height for the window
            primaryStage.show();                      // Makes the Stage (window) visible to the user

        } catch (Exception e) { // Error handling if the FXML file is not found or cannot be loaded/parsed
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }

    // launches the JavaFX application.
    public static void main(String[] args) {
        launch(args);
    }
}