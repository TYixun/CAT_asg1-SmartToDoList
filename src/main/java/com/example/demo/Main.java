package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 1200, 700);
            // Add CSS (new line)
            scene.getStylesheets().add(
                    getClass().getResource("/styles.css").toExternalForm()
            );

            // Setup stage
            primaryStage.setTitle("Smart Todo List");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}