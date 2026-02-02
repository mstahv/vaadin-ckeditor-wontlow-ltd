package com.wontlost.ckeditor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Utility class for JSON operations using Jackson.
 * Provides a shared ObjectMapper instance and common helper methods.
 *
 * <p>This class centralizes Jackson ObjectMapper usage to:
 * <ul>
 *   <li>Ensure consistent configuration across the codebase</li>
 *   <li>Avoid redundant ObjectMapper instances</li>
 *   <li>Provide convenient helper methods for common operations</li>
 * </ul>
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Get the shared ObjectMapper instance.
     * This instance is thread-safe for read operations.
     *
     * @return the shared ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Create a new ObjectNode.
     *
     * @return a new empty ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * Create a new ArrayNode.
     *
     * @return a new empty ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }

    /**
     * Convert a value to a JSON tree representation.
     *
     * @param value the value to convert
     * @param <T> the type of the value
     * @return the JSON tree representation
     */
    public static <T> JsonNode valueToTree(T value) {
        return MAPPER.valueToTree(value);
    }

    /**
     * Create an ArrayNode from a string array.
     * Null values in the array are skipped.
     *
     * @param items the string array
     * @return an ArrayNode containing the items, or an empty ArrayNode if items is null
     */
    public static ArrayNode toArrayNode(String[] items) {
        ArrayNode arr = MAPPER.createArrayNode();
        if (items != null) {
            for (String item : items) {
                if (item != null) {
                    arr.add(item);
                }
            }
        }
        return arr;
    }

    /**
     * Create an ArrayNode from a string array, only if the array is non-empty.
     *
     * @param items the string array
     * @return an ArrayNode containing the items, or null if items is null or empty
     */
    public static ArrayNode toArrayNodeOrNull(String[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        return toArrayNode(items);
    }

    /**
     * Check if an array is non-null and non-empty.
     *
     * @param items the array to check
     * @return true if the array has elements, false otherwise
     */
    public static boolean hasElements(Object[] items) {
        return items != null && items.length > 0;
    }
}
