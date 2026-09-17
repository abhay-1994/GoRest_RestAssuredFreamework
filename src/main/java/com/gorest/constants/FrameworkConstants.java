package com.gorest.constants;

/**
 * Framework-wide literals: config keys, folder names and default values.
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
    }

    /* ---------------- Files and folders ---------------- */
    public static final String CONFIG_FILE = "config.properties";
    public static final String PAYLOAD_FOLDER = "payloads/";
    public static final String SCHEMA_FOLDER = "schemas/";
    public static final String LOG_FILE = "target/logs/api.log";

    /* ---------------- Config keys ---------------- */
    public static final String BASE_URI = "base.uri";
    public static final String BASE_PATH = "base.path";
    public static final String TOKEN = "token";
    public static final String LOG_REQUESTS = "log.requests";
    public static final String MAX_RESPONSE_TIME_MS = "max.response.time.ms";

    /* ---------------- Domain values ---------------- */
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";
}
