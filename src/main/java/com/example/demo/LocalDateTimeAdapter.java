package com.example.demo;

import com.google.gson.*;                   // Gson library
import java.lang.reflect.Type;              // Used to represent the generic type information at runtime
import java.time.LocalDateTime;             // The specific Java 8 Date/Time object this adapter handles
import java.time.format.DateTimeFormatter;  // Used to define the specific format for converting LocalDateTime to and from a string

/*
    Custom Gson TypeAdapter for the Java 8 LocalDateTime class.
    This adapter is necessary because Gson does not natively know how to serialize
    or deserialize modern Java Time objects without custom configuration.
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    // Customize the datetime format to "YYYY-MM-DDTHH:MM:SS"
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Converts a LocalDateTime object into a JSON element (string primitive)
    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type typeOfSrc, JsonSerializationContext context) {
        // Serialize: LocalDateTime object -> JSON string
        return new JsonPrimitive(dateTime.format(formatter));
    }

    // Converts a JSON element (string primitive) back into a LocalDateTime object
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // Deserialize: JSON string -> LocalDateTime object
        return LocalDateTime.parse(json.getAsString(), formatter);
    }
}
