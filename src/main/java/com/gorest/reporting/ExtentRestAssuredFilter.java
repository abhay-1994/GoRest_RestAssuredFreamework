package com.gorest.reporting;

import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/** Adds a readable request and response entry to the current Extent test. */
public class ExtentRestAssuredFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        Response response = context.next(requestSpec, responseSpec);
        if (ExtentReportManager.getTest() == null) {
            return response;
        }

        ExtentReportManager.getTest().info(MarkupHelper.createCodeBlock(formatRequest(requestSpec)));
        ExtentReportManager.getTest().info(MarkupHelper.createCodeBlock(formatResponse(response)));
        return response;
    }

    private String formatRequest(FilterableRequestSpecification requestSpec) {
        StringBuilder request = new StringBuilder();
        request.append(requestSpec.getMethod()).append(" ").append(requestSpec.getURI()).append("\n");
        request.append("Headers:\n").append(formatHeaders(requestSpec.getHeaders())).append("\n");
        Object body = requestSpec.getBody();
        if (body != null) {
            request.append("Body:\n").append(String.valueOf((Object) body));
        }
        return request.toString();
    }

    private String formatResponse(Response response) {
        StringBuilder result = new StringBuilder();
        result.append("Status: ").append(response.getStatusCode())
                .append(" ").append(response.getStatusLine()).append("\n")
                .append("Time: ").append(response.getTime()).append(" ms\n")
                .append("Headers:\n").append(formatHeaders(response.getHeaders())).append("\n");

        String body = response.getBody().asPrettyString();
        if (!body.isBlank()) {
            result.append("Body:\n").append(body);
        }
        return result.toString();
    }

    private String formatHeaders(Headers headers) {
        StringBuilder result = new StringBuilder();
        for (Header header : headers) {
            if ("Authorization".equalsIgnoreCase(header.getName())) {
                result.append(header.getName()).append(": [hidden]\n");
            } else {
                result.append(header.getName()).append(": ").append(header.getValue()).append("\n");
            }
        }
        return result.toString();
    }
}
