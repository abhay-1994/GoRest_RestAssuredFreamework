package com.gorest.tests;

import com.gorest.base.AuthenticatedTest;
import com.gorest.constants.FrameworkConstants;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.pojo.User;
import com.gorest.utils.JsonUtility;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * UPDATE scenarios - PUT and PATCH on /users/{userId}. Needs a bearer token.
 */
public class UpdateUserTests extends AuthenticatedTest {

    @Test(groups = {"smoke", "regression"},
            description = "PUT /users/{userId} updates the user and returns 200 with the new values")
    public void updateUser_withPut_shouldReturn200() {
        User created = createUserThroughApi();

        User update = new User(TestDataUtility.randomName(), TestDataUtility.uniqueEmail(),
                FrameworkConstants.GENDER_FEMALE, FrameworkConstants.STATUS_INACTIVE);

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(update)
        .when()
                .put(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("id", equalTo(created.getId()))
                .body("name", equalTo(update.getName()))
                .body("email", equalTo(update.getEmail()))
                .body("gender", equalTo(update.getGender()))
                .body("status", equalTo(FrameworkConstants.STATUS_INACTIVE));
    }

    @Test(groups = {"regression"},
            description = "PATCH /users/{userId} updates only the field that was sent")
    public void updateUser_withPatch_shouldChangeOnlyThatField() {
        User created = createUserThroughApi();

        User statusOnly = new User().setStatus(FrameworkConstants.STATUS_INACTIVE);

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(statusOnly)
        .when()
                .patch(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("id", equalTo(created.getId()))
                .body("status", equalTo(FrameworkConstants.STATUS_INACTIVE))
                .body("name", equalTo(created.getName()))
                .body("email", equalTo(created.getEmail()));
    }

    @Test(groups = {"regression"},
            description = "PUT /users/{userId} works from the update-user.json payload template")
    public void updateUser_fromJsonPayloadTemplate_shouldReturn200() {
        User created = createUserThroughApi();

        String payload = JsonUtility.readPayload("update-user.json");
        payload = JsonUtility.updateField(payload, "name", TestDataUtility.randomName());
        final String expectedName = JsonUtility.getValue(payload, "name");

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(payload)
        .when()
                .put(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("name", equalTo(expectedName))
                .body("status", equalTo(FrameworkConstants.STATUS_INACTIVE));
    }

    @Test(groups = {"regression"},
            description = "The updated value is visible on a fresh GET /users/{userId}")
    public void updatedUser_shouldBePersisted() {
        User created = createUserThroughApi();
        String newName = TestDataUtility.randomName();

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(new User().setName(newName))
        .when()
                .patch(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK));

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .get(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("name", equalTo(newName));
    }

    @Test(groups = {"regression", "negative"},
            description = "PUT /users/{userId} with an unknown id returns 404")
    public void updateUser_withUnknownId_shouldReturn404() {
        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(TestDataUtility.randomUser())
        .when()
                .put(IEndpointLibrary.USER_BY_ID, TestDataUtility.nonExistingUserId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.NOT_FOUND))
                .body("message", equalTo("Resource not found"));
    }
}
