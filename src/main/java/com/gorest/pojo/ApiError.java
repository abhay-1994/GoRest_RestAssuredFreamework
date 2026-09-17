package com.gorest.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One entry of a GoREST 422 validation response, e.g.
 * {@code [{"field":"email","message":"has already been taken"}]}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError {

    private String field;
    private String message;

    public String getField() {
        return field;
    }

    public ApiError setField(String field) {
        this.field = field;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "ApiError{field='" + field + "', message='" + message + "'}";
    }
}
