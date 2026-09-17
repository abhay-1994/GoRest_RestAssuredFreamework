package com.gorest.tests;

import com.gorest.base.AuthenticatedTest;
import com.gorest.constants.IEndpointLibrary;
import com.gorest.constants.StatusCode;
import com.gorest.pojo.Post;
import com.gorest.pojo.User;
import com.gorest.utils.JsonUtility;
import com.gorest.utils.RestAssuredUtility;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;

/**
 * Nested resource scenarios - posts that belong to a user.
 */
public class UserPostTests extends AuthenticatedTest {

    @Test(groups = {"regression"},
            description = "POST /users/{userId}/posts creates a post linked to that user")
    public void createPostForUser_shouldReturn201() {
        User user = createUserThroughApi();
        Post post = TestDataUtility.randomPost();

        given()
                .spec(RestAssuredUtility.requestSpec())
                .body(post)
        .when()
                .post(IEndpointLibrary.USER_POSTS, user.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                .body("id", notNullValue())
                .body("user_id", equalTo(user.getId()))
                .body("title", equalTo(post.getTitle()))
                .body("body", equalTo(post.getBody()))
                .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
    }

    @Test(groups = {"regression"},
            description = "GET /users/{userId}/posts returns the posts created for that user")
    public void getPostsOfUser_shouldReturnCreatedPost() {
        User user = createUserThroughApi();

        String createdBody =
                given()
                        .spec(RestAssuredUtility.requestSpec())
                        .body(JsonUtility.readPayload("create-post.json"))
                .when()
                        .post(IEndpointLibrary.USER_POSTS, user.getId())
                .then()
                        .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
                        .extract()
                        .asString();

        Post created = JsonUtility.toObject(createdBody, Post.class);
        assertEquals(created.getUserId(), user.getId(), "Post should belong to the created user");

        given()
                .spec(RestAssuredUtility.requestSpec())
        .when()
                .get(IEndpointLibrary.USER_POSTS, user.getId())
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("size()", greaterThan(0))
                .body("user_id", everyItem(equalTo(user.getId())))
                .body("id", everyItem(notNullValue()));
    }

    @Test(groups = {"regression"},
            description = "GET /posts returns the public post feed")
    public void getAllPosts_shouldReturn200() {
        given()
                .spec(RestAssuredUtility.requestSpecWithoutAuth())
                .queryParam("per_page", 5)
        .when()
                .get(IEndpointLibrary.POSTS)
        .then()
                .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.OK))
                .body("size()", equalTo(5))
                .body("title", everyItem(notNullValue()));
    }
}
