package com.gorest.tests;

import com.gorest.base.BaseTest;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Authentication scenarios - every write call must be rejected with 401 when the
 * bearer token is missing or wrong. No token needed to run these.
 */
public class UnauthorizedAccessTests extends BaseTest {

    @Test(groups = {"smoke", "regression", "negative"},
            description = "POST /users without a token returns 401 Authentication failed")
    public void createUser_withoutToken_shouldReturn401() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .body(TestDataUtility.randomUser())
        .when()
                .post(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNAUTHORIZED))
                .body("message", equalTo("Authentication failed"));
    }

    @Test(groups = {"regression", "negative"},
            description = "POST /users with a bogus token returns 401 Invalid token")
    public void createUser_withInvalidToken_shouldReturn401() {
        given()
                .spec(RestAssuredUtility.requestSpecWithToken("invalid-token-" + System.currentTimeMillis()))
                .body(TestDataUtility.randomUser())
        .when()
                .post(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNAUTHORIZED))
                .body("message", equalTo("Invalid token"));
    }

    @Test(groups = {"regression", "negative"},
            description = "PUT /users/{userId} without a token returns 401")
    public void updateUser_withoutToken_shouldReturn401() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .body(TestDataUtility.randomUser())
        .when()
                .put(IEndpointLibrary.USER_BY_ID, anyExistingUserId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNAUTHORIZED))
                .body("message", equalTo("Authentication failed"));
    }

    @Test(groups = {"regression", "negative"},
            description = "DELETE /users/{userId} without a token returns 401")
    public void deleteUser_withoutToken_shouldReturn401() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, anyExistingUserId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNAUTHORIZED))
                .body("message", equalTo("Authentication failed"));
    }
}
