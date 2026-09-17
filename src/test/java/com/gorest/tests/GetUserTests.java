package com.gorest.tests;

import com.gorest.base.BaseTest;
import com.gorest.constants.FrameworkConstants;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.pojo.User;
import com.gorest.utils.JsonUtility;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * READ scenarios for /users. GoREST allows anonymous reads, so these run without a token.
 */
public class GetUserTests extends BaseTest {

    @Test(groups = {"smoke", "regression"},
            description = "GET /users returns 200 with a non-empty list matching the user schema")
    public void getAllUsers_shouldReturnUserList() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
        .when()
                .get(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("size()", greaterThan(0))
                .body("id", everyItem(notNullValue()))
                .body("email", everyItem(notNullValue()))
                .body(matchesJsonSchemaInClasspath("schemas/user-list-schema.json"));
    }

    @Test(groups = {"regression"},
            description = "GET /users honours page / per_page and returns the pagination headers")
    public void getUsers_shouldSupportPagination() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .queryParam("page", 2)
                .queryParam("per_page", 5)
        .when()
                .get(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("size()", equalTo(5))
                .header("x-pagination-page", equalTo("2"))
                .header("x-pagination-limit", equalTo("5"));
    }

    @Test(groups = {"regression"},
            description = "GET /users?status=inactive returns only inactive users")
    public void getUsers_shouldFilterByStatus() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .queryParam("status", FrameworkConstants.STATUS_INACTIVE)
                .queryParam("per_page", 10)
        .when()
                .get(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("status", everyItem(equalTo(FrameworkConstants.STATUS_INACTIVE)));
    }

    @Test(groups = {"regression"},
            description = "GET /users?gender=female returns only female users")
    public void getUsers_shouldFilterByGender() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .queryParam("gender", FrameworkConstants.GENDER_FEMALE)
                .queryParam("per_page", 10)
        .when()
                .get(IEndpointLibrary.USERS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("gender", everyItem(equalTo(FrameworkConstants.GENDER_FEMALE)));
    }

    @Test(groups = {"smoke", "regression"},
            description = "GET /users/{userId} returns the requested user")
    public void getUserById_shouldReturnThatUser() {
        Integer userId = anyExistingUserId();

        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
        .when()
                .get(IEndpointLibrary.USER_BY_ID, userId)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("id", equalTo(userId))
                .body("name", notNullValue())
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    @Test(groups = {"regression", "negative"},
            description = "GET /users/{userId} with an unknown id returns 404 Resource not found")
    public void getUserById_withUnknownId_shouldReturn404() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
        .when()
                .get(IEndpointLibrary.USER_BY_ID, TestDataUtility.nonExistingUserId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.NOT_FOUND))
                .body("message", equalTo("Resource not found"));
    }

    @Test(groups = {"regression"},
            description = "The user list deserialises cleanly into User POJOs via JsonUtility")
    public void getUsers_shouldDeserialiseIntoPojos() {
        Response response =
                given()
                        .spec(RestAssuredUtility.requestSpecWithoutAuth())
                        .queryParam("per_page", 5)
                .when()
                        .get(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                        .extract()
                        .response();

        List<User> users = JsonUtility.toList(response, User.class);

        assertFalse(users.isEmpty(), "User list should not be empty");
        assertEquals(users.size(), 5, "per_page=5 should return 5 users");
        users.forEach(user -> {
            assertNotNull(user.getId(), "User id should be present");
            assertNotNull(user.getEmail(), "User email should be present");
        });
    }

    @Test(groups = {"regression"},
            description = "x-pagination-total reports how many users exist")
    public void getUsers_shouldExposeTotalCountHeader() {
        String total =
                given()
                        .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .when()
                        .get(IEndpointLibrary.USERS)
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                        .extract()
                        .header("x-pagination-total");

        assertThat("Total user count", Integer.parseInt(total), greaterThan(0));
    }
}
