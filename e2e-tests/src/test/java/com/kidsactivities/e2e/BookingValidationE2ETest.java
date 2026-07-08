package com.kidsactivities.e2e;

import com.kidsactivities.e2e.support.ApiClient;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("e2e")
@DisplayName("US-07 / US-09 / US-13 — Cas limites réservations & activités")
class BookingValidationE2ETest {

    private static String baseUrl;
    private static String parentToken;
    private static String adminToken;

    @BeforeAll
    static void setup() {
        baseUrl = System.getProperty("e2e.base.url", "http://localhost:8080");
        parentToken = ApiClient.login(baseUrl, "parent@example.com", "parent123");
        adminToken = ApiClient.login(baseUrl, "admin@kidsactivities.fr", "admin123");
    }

    @Test
    @DisplayName("US-07: Given booking invalid payload When POST Then 400 validation")
    void us07_rejectInvalidChildAge() {
        Long activityId = given()
                .baseUri(baseUrl)
                .when()
                .get("/api/activities")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "activityId", activityId,
                        "childName", "",
                        "childAge", 0
                ))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("US-09: Given unknown booking When DELETE Then 404")
    void us09_rejectUnknownBooking() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .delete("/api/bookings/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("US-13: Given capacity below booked When PUT activity Then 400")
    void us13_rejectCapacityBelowBooked() {
        Long activityId = given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "QA Capacité " + UUID.randomUUID(),
                        "description", "Test capacité",
                        "startDateTime", LocalDateTime.now().plusDays(20).withNano(0).toString(),
                        "location", "Nantes",
                        "maxCapacity", 2,
                        "price", 0,
                        "active", true
                ))
                .when()
                .post("/api/activities")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String secondParentToken = ApiClient.register(
                baseUrl,
                "qa-parent-" + UUID.randomUUID() + "@example.com",
                "QA",
                "Parent"
        );

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + secondParentToken)
                .contentType(ContentType.JSON)
                .body(Map.of("activityId", activityId, "childName", "A", "childAge", 7))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201);

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .contentType(ContentType.JSON)
                .body(Map.of("activityId", activityId, "childName", "B", "childAge", 8))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201);

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "QA Capacité modifiée",
                        "description", "Test",
                        "startDateTime", LocalDateTime.now().plusDays(20).withNano(0).toString(),
                        "location", "Nantes",
                        "maxCapacity", 1,
                        "price", 0,
                        "active", true
                ))
                .when()
                .put("/api/activities/{id}", activityId)
                .then()
                .statusCode(400)
                .body("message", containsString("capacité"));
    }

    @Test
    @DisplayName("US-08: Given parent When GET bookings Then only own bookings")
    void us08_parentBookingsContainOwnEmail() {
        List<Map<String, Object>> bookings = given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        for (Map<String, Object> booking : bookings) {
            if (booking.get("userEmail") != null) {
                org.assertj.core.api.Assertions.assertThat(booking.get("userEmail"))
                        .isEqualTo("parent@example.com");
            }
        }
    }
}
