package com.gorest.base;

import com.gorest.config.ConfigManager;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.pojo.User;
import com.gorest.utils.LogUtility;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * Parent of every test class.
 *
 * <p>Boots REST Assured once per suite and offers the small set of helpers tests need
 * for their fixtures: create a user, find an existing user, clean up afterwards.</p>
 */
public class BaseTest {

    /** Users created by the running test class, deleted in {@link #cleanUpCreatedUsers()}. */
    private final List<Integer> createdUserIds = new ArrayList<>();

    @BeforeSuite(alwaysRun = true)
    public void initialiseFramework() {
        RestAssuredUtility.initialize();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpCreatedUsers() {
        if (!ConfigManager.isTokenConfigured()) {
            return;
        }
        createdUserIds.forEach(this::deleteUserQuietly);
        createdUserIds.clear();
    }

    /**
     * Creates a random user through the API and registers it for cleanup.
     *
     * @return the user with the id assigned by the server
     */
    protected User createUserThroughApi() {
        User user = TestDataUtility.randomUser();

        Integer userId =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(user)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .extract()
                        .path("id");

        user.setId(userId);
        trackForCleanup(userId);
        LogUtility.info("Test data created -> " + user);
        return user;
    }

    /** Marks a user id so it is removed after the class finishes. */
    protected void trackForCleanup(Integer userId) {
        if (userId != null) {
            createdUserIds.add(userId);
        }
    }

    /** Stops tracking a user the test itself already deleted. */
    protected void untrack(Integer userId) {
        createdUserIds.remove(userId);
    }

    /** Id of any user already present on the server - used by read-only tests. */
    protected Integer anyExistingUserId() {
        return given()
                        .spec(RestAssuredUtility.requestSpecWithoutAuth())
                        .queryParam("page", 1)
                        .queryParam("per_page", 1)
                .when()
                        .get(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                        .extract()
                        .path("[0].id");
    }

    /** Best-effort delete used for cleanup - never fails a test. */
    protected void deleteUserQuietly(Integer userId) {
        try {
            given()
                    .spec(RestAssuredUtility.requestSpec())
            .when()
                    .delete(IEndpointLibrary.USER_BY_ID, userId)
            .then()
                    .statusCode(anyOf(is(StatusCode.NO_CONTENT.code()), is(StatusCode.NOT_FOUND.code())));

            LogUtility.info("Test data cleaned up -> user " + userId);
        } catch (RuntimeException e) {
            LogUtility.warn("Cleanup of user " + userId + " failed: " + e.getMessage());
        }
    }
}
