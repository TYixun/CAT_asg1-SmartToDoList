package com.example.demo;

import com.google.gson.Gson;                // Core Gson library class for JSON serialization/deserialization
import com.google.gson.GsonBuilder;         // Utility class to construct and configure Gson instances
import com.google.gson.reflect.TypeToken;   // Used to capture generic type information for deserializing List<Task>
import com.example.demo.model.Task;         // Import the Task model class you want to save/load
import java.io.*;                           // Package containing classes for input/output operations (File, Reader, Writer)
import java.lang.reflect.Type;              // Used with TypeToken to represent generic type structure at runtime
import java.time.LocalDate;                 // Used to handle the dueDate field
import java.time.LocalDateTime;             // Used to handle the createdAt field
import java.util.ArrayList;                 // Used for creating an initial, empty list of tasks
import java.util.List;                      // Used for the collection of tasks

// Utility class for handling JSON serialization and deserialization of a list of Task objects
public class JsonUtil {
    private static final String FILE_PATH = "tasks.json"; // The name of the file used for persistence

    // Configured Gson instance, built using GsonBuilder
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting() // Formats the JSON output for human readability (nice spacing/newlines)
            // Custom type adapters are registered here to correctly handle Java 8 Time API objects (LocalDate, LocalDateTime)
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // Save tasks to JSON file
    public static void saveTasks(List<Task> tasks) {
        // Use try-with-resources to ensure the Writer is closed automatically
        try (Writer writer = new FileWriter(FILE_PATH)) {
            // Converts the List<Task> object into a JSON string and writes it to the file
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved successfully!");
        } catch (IOException e) { // Handle file I/O errors
            System.err.println("Error saving tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load tasks from JSON file
    public static List<Task> loadTasks() {
        File file = new File(FILE_PATH);

        // 1. Check if the file is missing
        if (!file.exists()) {
            System.out.println("No existing tasks file found. Starting with empty list.");
            return new ArrayList<>();
        }

        // 2. Check if the file is empty (0 bytes)
        if (file.length() == 0) {
            System.out.println("Tasks file is empty. Starting with empty list.");
            return new ArrayList<>();
        }

        // Use try-with-resources to ensure the Reader is closed automatically
        try (Reader reader = new FileReader(FILE_PATH)) {
            // Define a TypeToken to correctly handle the generic type List<Task> during deserialization
            // This is necessary because Java's type erasure loses generic type info at runtime
            Type taskListType = new TypeToken<ArrayList<Task>>() {
            }.getType();

            // Deserialization with Gson: reads the JSON from the file and converts it back to List<Task>
            List<Task> tasks = gson.fromJson(reader, taskListType);

            if (tasks == null) {
                // This handles cases where the file might contain 'null' text
                return new ArrayList<>();
            }

            System.out.println("Tasks loaded successfully! Count: " + tasks.size());
            return tasks;
        } catch (IOException e) { // Catch for issues like inability to read the file
            System.err.println("Error loading tasks (IO): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            // Catch for corrupt JSON data (e.g., missing brackets, invalid format)
            System.err.println("Error loading tasks (Syntax: JSON is corrupt/invalid): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
}