package com.kidsactivities.e2e.support;

import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class ApiClient {

    private ApiClient() {
    }

    public static String login(String baseUrl, String email, String password) {
        return given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    public static String register(String baseUrl, String email, String firstName, String lastName) {
        return given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "password123",
                        "firstName", firstName,
                        "lastName", lastName
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .extract()
                .path("token");
    }
}
