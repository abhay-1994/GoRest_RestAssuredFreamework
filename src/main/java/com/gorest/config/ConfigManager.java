package com.gorest.config;

import com.gorest.constants.FrameworkConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads {@code src/main/resources/config.properties}.
 *
 * <p>Look-up order for every key, so nothing secret has to be committed:</p>
 * <ol>
 *     <li>System property  - {@code mvn test -Dtoken=xxxx}</li>
 *     <li>Environment var  - {@code TOKEN=xxxx} (dots become underscores, upper-cased)</li>
 *     <li>config.properties</li>
 * </ol>
 */
public final class ConfigManager {

    private static final Properties PROPERTIES = load();

    private ConfigManager() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = ConfigManager.class.getClassLoader()
                .getResourceAsStream(FrameworkConstants.CONFIG_FILE)) {

            if (stream == null) {
                throw new IllegalStateException(FrameworkConstants.CONFIG_FILE + " not found on the classpath");
            }
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + FrameworkConstants.CONFIG_FILE, e);
        }
        return properties;
    }

    /**
     * @return value for the key, or {@code null} when it is not configured anywhere.
     */
    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (isBlank(value)) {
            value = PROPERTIES.getProperty(key);
        }
        return isBlank(value) ? null : value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /* ---------------- Typed shortcuts used by the framework ---------------- */

    public static String baseUri() {
        return get(FrameworkConstants.BASE_URI, "https://gorest.co.in");
    }

    public static String basePath() {
        return get(FrameworkConstants.BASE_PATH, "/public/v2");
    }

    public static String token() {
        return get(FrameworkConstants.TOKEN);
    }

    /**
     * Auth-protected tests are skipped (not failed) when no token is supplied.
     */
    public static boolean isTokenConfigured() {
        String token = token();
        return token != null && !token.startsWith("PUT_YOUR");
    }

    public static boolean logRequests() {
        return getBoolean(FrameworkConstants.LOG_REQUESTS, true);
    }

    public static long maxResponseTimeMs() {
        return getInt(FrameworkConstants.MAX_RESPONSE_TIME_MS, 5000);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
