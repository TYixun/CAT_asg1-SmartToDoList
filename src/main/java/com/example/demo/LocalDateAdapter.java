package com.example.demo;

import com.google.gson.*;                   // Gson library
import java.lang.reflect.Type;              // Used to represent the generic type of the object being serialized/deserialized
import java.time.LocalDate;                 // The specific Java 8 Date/Time object this adapter handles
import java.time.format.DateTimeFormatter;  // Used to define the specific format for converting LocalDate to and from a string

/*
    Custom Gson TypeAdapter for the Java 8 LocalDate class.
    This adapter tells Gson how to correctly serialize a LocalDate object into a JSON string
    and deserialize a JSON string back into a LocalDate object, using a defined format.
*/
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    // Customize the date format. ISO_LOCAL_DATE standardizes the format to "YYYY-MM-DD"
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // Called by Gson during serialization to convert a LocalDate object into a JSON element
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        // Serialize: LocalDate object -> JSON string
        return new JsonPrimitive(date.format(formatter));
    }

    // Called by Gson during deserialization to convert a JSON element into a LocalDate object
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Deserialize: JSON string -> LocalDate object
        return LocalDate.parse(json.getAsString(), formatter);
    }
}