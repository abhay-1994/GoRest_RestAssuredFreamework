package com.gorest.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gorest.constants.FrameworkConstants;
import io.restassured.response.Response;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Everything JSON in one place: POJO to JSON, JSON to POJO, reading payload
 * templates from the classpath and tweaking a single field inside them.
 */
public final class JsonUtility {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final ObjectWriter PRETTY_WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    private JsonUtility() {
    }

    /* ------------------------------------------------------------------
     *  Serialise / deserialise
     * ------------------------------------------------------------------ */

    /** POJO or Map -> JSON string. */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert object to JSON: " + object, e);
        }
    }

    /** JSON string -> POJO. */
    public static <T> T toObject(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert JSON to " + type.getSimpleName() + ": " + json, e);
        }
    }

    /** Response body -> POJO. */
    public static <T> T toObject(Response response, Class<T> type) {
        return toObject(response.asString(), type);
    }

    /** JSON array string -> List of POJOs. */
    public static <T> List<T> toList(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert JSON array to List<" + type.getSimpleName() + ">", e);
        }
    }

    /** Response body (JSON array) -> List of POJOs. */
    public static <T> List<T> toList(Response response, Class<T> type) {
        return toList(response.asString(), type);
    }

    /** JSON string -> Map, when a POJO would be overkill. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        return toObject(json, Map.class);
    }

    /* ------------------------------------------------------------------
     *  Payload templates
     * ------------------------------------------------------------------ */

    /**
     * Reads a payload template from {@code src/main/resources/payloads/}.
     *
     * @param fileName e.g. {@code create-user.json}
     */
    public static String readPayload(String fileName) {
        return readFromClasspath(FrameworkConstants.PAYLOAD_FOLDER + fileName);
    }

    /** Reads any JSON file sitting on the classpath. */
    public static String readFromClasspath(String path) {
        try (InputStream stream = JsonUtility.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("File not found on classpath: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    /**
     * Replaces a top-level field in a JSON string and returns the updated JSON,
     * e.g. making a template payload unique before sending it.
     */
    public static String updateField(String json, String field, Object value) {
        try {
            ObjectNode node = (ObjectNode) MAPPER.readTree(json);
            node.set(field, MAPPER.valueToTree(value));
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Could not update field '" + field + "' in JSON: " + json, e);
        }
    }

    /** Removes a top-level field - useful for "mandatory field missing" tests. */
    public static String removeField(String json, String field) {
        try {
            ObjectNode node = (ObjectNode) MAPPER.readTree(json);
            node.remove(field);
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Could not remove field '" + field + "' from JSON: " + json, e);
        }
    }

    /* ------------------------------------------------------------------
     *  Reading values out of JSON
     * ------------------------------------------------------------------ */

    /**
     * Pulls a value out of a JSON string with a GPath expression,
     * e.g. {@code getValue(body, "name")} or {@code getValue(body, "[0].email")}.
     */
    public static String getValue(String json, String jsonPath) {
        return io.restassured.path.json.JsonPath.from(json).getString(jsonPath);
    }

    /** Formats JSON for readable log output. */
    public static String prettyPrint(String json) {
        try {
            return PRETTY_WRITER.writeValueAsString(MAPPER.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }
}
