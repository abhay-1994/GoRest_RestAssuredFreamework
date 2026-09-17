package com.gorest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin wrapper over SLF4J so a test can log a step with one static call.
 */
public final class LogUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger("GoREST");

    private LogUtility() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }
}
