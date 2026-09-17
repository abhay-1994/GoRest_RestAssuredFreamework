package com.gorest.tests;

import com.gorest.base.AuthenticatedTest;
import com.gorest.constants.FrameworkConstants;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.data.UserDataProvider;
import com.gorest.pojo.User;
import com.gorest.utils.JsonUtility;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * CREATE scenarios - POST /users. Needs a bearer token.
 */
public class CreateUserTests extends AuthenticatedTest {

    @Test(groups = {"smoke", "regression"},
            description = "POST /users with a valid payload returns 201 and echoes the data back")
    public void createUser_withValidPayload_shouldReturn201() {
        User user = TestDataUtility.randomUser();

        Integer userId =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(user)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .body("id", notNullValue())
                        .body("id", greaterThan(0))
                        .body("name", equalTo(user.getName()))
                        .body("email", equalTo(user.getEmail()))
                        .body("gender", equalTo(user.getGender()))
                        .body("status", equalTo(user.getStatus()))
                        .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
                        .extract()
                        .path("id");

        trackForCleanup(userId);
    }

    @Test(groups = {"regression"},
            description = "A user created through POST can be fetched back with GET /users/{userId}")
    public void createdUser_shouldBeRetrievableById() {
        User created = createUserThroughApi();

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .get(IEndpointLibrary.USER_BY_ID, created.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("id", equalTo(created.getId()))
                .body("email", equalTo(created.getEmail()))
                .body("status", equalTo(FrameworkConstants.STATUS_ACTIVE));
    }

    @Test(groups = {"regression"},
            description = "POST /users works from a JSON template loaded by JsonUtility")
    public void createUser_fromJsonPayloadTemplate_shouldReturn201() {
        String payload = JsonUtility.readPayload("create-user.json");
        payload = JsonUtility.updateField(payload, "email", TestDataUtility.uniqueEmail());
        payload = JsonUtility.updateField(payload, "name", TestDataUtility.randomName());

        final String expectedEmail = JsonUtility.getValue(payload, "email");
        final String expectedName = JsonUtility.getValue(payload, "name");

        Integer userId =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(payload)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .body("email", equalTo(expectedEmail))
                        .body("name", equalTo(expectedName))
                        .extract()
                        .path("id");

        trackForCleanup(userId);
    }

    @Test(groups = {"regression"},
            dataProvider = "validUsers", dataProviderClass = UserDataProvider.class,
            description = "POST /users accepts every supported gender and status combination")
    public void createUser_withSupportedCombinations_shouldReturn201(String scenario, User user) {
        Integer userId =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(user)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .body("gender", equalTo(user.getGender()))
                        .body("status", equalTo(user.getStatus()))
                        .extract()
                        .path("id");

        trackForCleanup(userId);
    }

    @Test(groups = {"regression"},
            description = "The 201 response body deserialises back into a User POJO")
    public void createUser_responseShouldDeserialiseIntoPojo() {
        User request = TestDataUtility.randomUser();

        String body =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(request)
                .when()
                        .post(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .extract()
                        .asString();

        User created = JsonUtility.toObject(body, User.class);
        trackForCleanup(created.getId());

        assertNotNull(created.getId(), "Created user should carry an id");
        assertEquals(created.getEmail(), request.getEmail(), "Email should round-trip unchanged");
    }
}
