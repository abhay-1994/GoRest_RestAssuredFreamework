package com.gorest.constants;

/**
 * Single place where every GoREST endpoint path lives.
 *
 * <p>Tests never hard-code a URL - they refer to a constant from here, so a path change
 * is a one-line edit in this file.</p>
 *
 * <p>Paths use REST Assured path parameters ({@code {userId}}), e.g.
 * {@code .get(IEndpointLibrary.USER_BY_ID, userId)}.</p>
 */
public interface IEndpointLibrary {

    /* ---------------- Users ---------------- */
    String USERS = "/users";
    String USER_BY_ID = "/users/{userId}";

    /* ---------------- Posts ---------------- */
    String POSTS = "/posts";
    String POST_BY_ID = "/posts/{postId}";
    String USER_POSTS = "/users/{userId}/posts";

    /* ---------------- Comments ---------------- */
    String COMMENTS = "/comments";
    String POST_COMMENTS = "/posts/{postId}/comments";

    /* ---------------- Todos ---------------- */
    String TODOS = "/todos";
    String USER_TODOS = "/users/{userId}/todos";
}
