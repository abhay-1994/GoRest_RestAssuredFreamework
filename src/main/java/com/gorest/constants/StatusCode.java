package com.gorest.constants;

/**
 * HTTP status codes used by the GoREST API, so tests read
 * {@code StatusCode.CREATED.code()} instead of a bare 201.
 */
public enum StatusCode {

    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    UNPROCESSABLE_ENTITY(422);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
