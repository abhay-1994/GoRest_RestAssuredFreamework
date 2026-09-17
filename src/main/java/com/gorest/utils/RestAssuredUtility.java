package com.gorest.utils;

import com.gorest.config.ConfigManager;
import com.gorest.constants.FrameworkConstants;
import com.gorest.constants.StatusCode;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;

/**
 * Central REST Assured setup: base URI, headers, auth and the reusable
 * request / response specifications every test plugs into.
 *
 * <pre>
 * given()
 *     .spec(RestAssuredUtility.requestSpec())
 * .when()
 *     .get(IEndpointLibrary.USERS)
 * .then()
 *     .spec(RestAssuredUtility.responseSpec(StatusCode.OK));
 * </pre>
 */
public final class RestAssuredUtility {

    private static boolean initialised = false;

    private RestAssuredUtility() {
    }

    /**
     * Called once per suite from the base test. Sets base URI / base path and
     * attaches the request-response logging filters.
     */
    public static synchronized void initialize() {
        if (initialised) {
            return;
        }
        RestAssured.baseURI = ConfigManager.baseUri();
        RestAssured.basePath = ConfigManager.basePath();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        if (ConfigManager.logRequests()) {
            PrintStream logStream = logStream();
            RestAssured.filters(new RequestLoggingFilter(logStream), new ResponseLoggingFilter(logStream));
        }
        initialised = true;
        LogUtility.info("REST Assured initialised -> " + RestAssured.baseURI + RestAssured.basePath);
    }

    /* ------------------------------------------------------------------
     *  Request specifications
     * ------------------------------------------------------------------ */

    /**
     * JSON request carrying the bearer token - used by POST / PUT / PATCH / DELETE.
     */
    public static RequestSpecification requestSpec() {
        return requestSpecWithToken(ConfigManager.token());
    }

    /**
     * JSON request with no Authorization header - GoREST allows anonymous reads.
     */
    public static RequestSpecification requestSpecWithoutAuth() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /**
     * JSON request with a caller-supplied token - handy for negative auth tests.
     */
    public static RequestSpecification requestSpecWithToken(String token) {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    /* ------------------------------------------------------------------
     *  Response specifications
     * ------------------------------------------------------------------ */

    /**
     * Asserts the status code and that the call stayed inside the response-time budget.
     * Use this for 204 responses, which carry no body.
     */
    public static ResponseSpecification responseSpec(int expectedStatusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .expectResponseTime(lessThan(ConfigManager.maxResponseTimeMs()), TimeUnit.MILLISECONDS)
                .build();
    }

    public static ResponseSpecification responseSpec(StatusCode expectedStatus) {
        return responseSpec(expectedStatus.code());
    }

    /**
     * Same as {@link #responseSpec(int)} plus a JSON content-type check.
     */
    public static ResponseSpecification jsonResponseSpec(int expectedStatusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatusCode)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(ConfigManager.maxResponseTimeMs()), TimeUnit.MILLISECONDS)
                .build();
    }

    public static ResponseSpecification jsonResponseSpec(StatusCode expectedStatus) {
        return jsonResponseSpec(expectedStatus.code());
    }

    /* ------------------------------------------------------------------
     *  Internals
     * ------------------------------------------------------------------ */

    private static PrintStream logStream() {
        try {
            File logFile = new File(FrameworkConstants.LOG_FILE);
            File parent = logFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Could not create log folder " + parent);
            }
            return new PrintStream(new FileOutputStream(logFile, true), true, "UTF-8");
        } catch (Exception e) {
            LogUtility.info("File logging unavailable (" + e.getMessage() + "), falling back to console");
            return System.out;
        }
    }
}
