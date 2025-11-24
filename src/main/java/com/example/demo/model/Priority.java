package com.example.demo.model;

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    // constant integer to store priority level
    private final int level;

    // Constructor to assign priority level
    Priority(int level) {
        this.level = level;
    }

    // Getter for priority level
    public int getLevel() {
        return level;
    }
}

