package com.gorest.tests;

import com.gorest.base.AuthenticatedTest;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.pojo.User;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;

/**
 * DELETE scenarios - DELETE /users/{userId}. Needs a bearer token.
 */
public class DeleteUserTests extends AuthenticatedTest {

    @Test(groups = {"smoke", "regression"},
            description = "DELETE /users/{userId} removes the user and returns 204 with no body")
    public void deleteUser_shouldReturn204() {
        User created = createUserThroughApi();

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.responseSpec(StatusCode.NO_CONTENT))
                .body(emptyOrNullString());

        untrack(created.getId());
    }

    @Test(groups = {"regression"},
            description = "A deleted user can no longer be fetched")
    public void deletedUser_shouldNotBeRetrievable() {
        User created = createUserThroughApi();

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.responseSpec(StatusCode.NO_CONTENT));

        untrack(created.getId());

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .get(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.NOT_FOUND))
                .body("message", equalTo("Resource not found"));
    }

    @Test(groups = {"regression", "negative"},
            description = "Deleting the same user twice returns 404 the second time")
    public void deleteUser_twice_shouldReturn404() {
        User created = createUserThroughApi();

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.responseSpec(StatusCode.NO_CONTENT));

        untrack(created.getId());

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.NOT_FOUND))
                .body("message", equalTo("Resource not found"));
    }

    @Test(groups = {"regression", "negative"},
            description = "DELETE /users/{userId} with an unknown id returns 404")
    public void deleteUser_withUnknownId_shouldReturn404() {
        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .delete(IEndpointLibrary.USER_BY_ID, TestDataUtility.nonExistingUserId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.NOT_FOUND))
                .body("message", equalTo("Resource not found"));
    }
}
