package com.ems.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for test data serialization and deserialization
 */
public final class TestSerializer {

    private static final Logger logger = LoggerFactory.getLogger(TestSerializer.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private TestSerializer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Create configured ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Serialize object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Deserialize JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to object", e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    /**
     * Save object to JSON file
     */
    public static void saveToFile(Object object, Path filePath) {
        try {
            String json = toJson(object);
            Files.writeString(filePath, json);
            logger.debug("Saved object to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save object to file", e);
            throw new RuntimeException("File write failed", e);
        }
    }

    /**
     * Load object from JSON file
     */
    public static <T> T loadFromFile(Path filePath, Class<T> type) {
        try {
            String json = Files.readString(filePath);
            T object = fromJson(json, type);
            logger.debug("Loaded object from file: {}", filePath);
            return object;
        } catch (IOException e) {
            logger.error("Failed to load object from file", e);
            throw new RuntimeException("File read failed", e);
        }
    }

    /**
     * Pretty print JSON
     */
    public static String prettyPrint(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to pretty print object", e);
            throw new RuntimeException("Pretty print failed", e);
        }
    }

    /**
     * Clone object through serialization
     */
    public static <T> T deepClone(T object, Class<T> type) {
        return fromJson(toJson(object), type);
    }

    /**
     * Get ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
