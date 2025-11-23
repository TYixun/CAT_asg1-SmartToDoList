package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.example.demo.model.Task;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final String FILE_PATH = "tasks.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // Save tasks to JSON file
    public static void saveTasks(List<Task> tasks) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved successfully!");
        } catch (IOException e) {
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

        try (Reader reader = new FileReader(FILE_PATH)) {
            // Define TypeToken
            Type taskListType = new TypeToken<ArrayList<Task>>() {
            }.getType();

            // Deserialization with Gson
            List<Task> tasks = gson.fromJson(reader, taskListType);

            if (tasks == null) {
                // This handles cases where the file might contain 'null' text
                return new ArrayList<>();
            }

            System.out.println("Tasks loaded successfully! Count: " + tasks.size());
            return tasks;
        } catch (IOException e) {
            System.err.println("Error loading tasks (IO): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            // Catch for corrupt JSON
            System.err.println("Error loading tasks (Syntax: JSON is corrupt/invalid): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
}