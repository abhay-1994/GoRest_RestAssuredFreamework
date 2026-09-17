package com.gorest.tests;

import com.gorest.base.AuthenticatedTest;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.data.UserDataProvider;
import com.gorest.pojo.ApiError;
import com.gorest.pojo.User;
import com.gorest.utils.JsonUtility;
import com.gorest.utils.RestAssuredUtility;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Negative CREATE scenarios - the API must answer 422 with a field-level error list.
 */
public class UserValidationTests extends AuthenticatedTest {

    @Test(groups = {"regression", "negative"},
            dataProvider = "invalidUsers", dataProviderClass = UserDataProvider.class,
            description = "POST /users with invalid data returns 422 naming the offending field")
    public void createUser_withInvalidData_shouldReturn422(String scenario, User user, String expectedField) {
        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(user)
        .when()
                .post(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNPROCESSABLE_ENTITY))
                .body("field", hasItem(expectedField))
                .body("message", hasItem(notNullValue()));
    }

    @Test(groups = {"smoke", "regression", "negative"},
            description = "POST /users with an email that already exists returns 422")
    public void createUser_withDuplicateEmail_shouldReturn422() {
        User existing = createUserThroughApi();

        User duplicate = new User("Duplicate Email User", existing.getEmail(),
                existing.getGender(), existing.getStatus());

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(duplicate)
        .when()
                .post(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNPROCESSABLE_ENTITY))
                .body("field", hasItem("email"))
                .body("message", hasItem(containsString("has already been taken")));
    }

    @Test(groups = {"regression", "negative"},
            description = "POST /users with every field blank returns one 422 error per field")
    public void createUser_withBlankPayload_shouldReturn422ForEachField() {
        String payload = JsonUtility.readPayload("invalid-user.json");

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(payload)
        .when()
                .post(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNPROCESSABLE_ENTITY))
                .body("field", hasItems("name", "email"))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test(groups = {"regression", "negative"},
            description = "POST /users without the email field returns 422 and maps to ApiError POJOs")
    public void createUser_withoutEmailField_shouldReturn422() {
        String payload = JsonUtility.removeField(JsonUtility.readPayload("create-user.json"), "email");

        String body =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(payload)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.UNPROCESSABLE_ENTITY))
                        .extract()
                        .asString();

        List<ApiError> errors = JsonUtility.toList(body, ApiError.class);

        assertThat("Validation errors", errors, hasSize(greaterThanOrEqualTo(1)));
        assertThat("Field reported as invalid",
                errors.stream().map(ApiError::getField).toList(), hasItem("email"));
    }
}
