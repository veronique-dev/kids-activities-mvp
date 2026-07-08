package com.kidsactivities.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'acceptation alignés sur les User Stories Must.
 * Prérequis : stack démarrée (docker compose up) sur http://localhost:8080
 *
 * mvn -pl e2e-tests test -De2e.base.url=http://localhost:8080
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MVP — Parcours principal (User Stories Must)")
class MvpAcceptanceTest {

    private static String baseUrl;
    private static String parentToken;
    private static String adminToken;
    private static Long activityId;
    private static Long bookingId;
    private static Long createdActivityId;

    @BeforeAll
    static void setup() {
        baseUrl = System.getProperty("e2e.base.url", "http://localhost:8080");
    }

    @Test
    @Order(1)
    void us01_publicActivitiesList() {
        List<Map<String, Object>> activities = given()
                .baseUri(baseUrl)
                .when()
                .get("/api/activities")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        assertThat(activities).isNotEmpty();
        assertThat(activities.getFirst()).containsKeys("title", "availableSpots", "price");
        activityId = ((Number) activities.getFirst().get("id")).longValue();
    }

    @Test
    @Order(2)
    void us02_activityDetail() {
        given()
                .baseUri(baseUrl)
                .when()
                .get("/api/activities/{id}", activityId)
                .then()
                .statusCode(200)
                .body("title", notNullValue())
                .body("location", notNullValue())
                .body("price", notNullValue());
    }

    @Test
    @Order(3)
    void us04_loginValidAndInvalid() {
        parentToken = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "parent@example.com",
                        "password", "parent123"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", not(emptyString()))
                .extract()
                .path("token");

        given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "parent@example.com",
                        "password", "wrong-password"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("US-06: Given parent token When GET /users/me Then profile")
    void us06_getCurrentUserProfile() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .body("email", equalTo("parent@example.com"))
                .body("role", equalTo("PARENT"));
    }

    @Test
    @Order(5)
    @DisplayName("US-03: Given unique email When register Then 201; Given duplicate Then 400")
    void us03_registerAndDuplicateEmail() {
        String uniqueEmail = "e2e-" + UUID.randomUUID() + "@example.com";

        given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", uniqueEmail,
                        "password", "password123",
                        "firstName", "E2E",
                        "lastName", "Test"
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("token", not(emptyString()))
                .body("user.role", equalTo("PARENT"));

        given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "parent@example.com",
                        "password", "password123",
                        "firstName", "Dup",
                        "lastName", "Licata"
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("déjà utilisé"));
    }

    @Test
    @Order(6)
    @DisplayName("US-20: Given no/invalid token When protected route Then 401")
    void us20_jwtSecurity() {
        given()
                .baseUri(baseUrl)
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(401);

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(7)
    @DisplayName("US-07: Given parent When book Then PENDING_PAYMENT then pay Then CONFIRMED")
    void us07_createBooking() {
        bookingId = given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "activityId", activityId,
                        "childName", "Lucas",
                        "childAge", 8
                ))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING_PAYMENT"))
                .body("paymentRequired", equalTo(true))
                .body("childName", equalTo("Lucas"))
                .extract()
                .path("id");

        Long paymentId = given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .contentType(ContentType.JSON)
                .body(Map.of("bookingId", bookingId))
                .when()
                .post("/api/payments/checkout")
                .then()
                .statusCode(200)
                .body("provider", equalTo("MOCK"))
                .extract()
                .path("paymentId");

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .post("/api/payments/{id}/complete-mock", paymentId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));

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

        assertThat(bookings.stream()
                .filter(b -> bookingId.equals(((Number) b.get("id")).longValue()))
                .findFirst()
                .orElseThrow()
                .get("status")).isEqualTo("CONFIRMED");
    }

    @Test
    @Order(8)
    @DisplayName("US-07: Given existing booking When book same activity Then 400")
    void us07_duplicateBookingRejected() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "activityId", activityId,
                        "childName", "Emma",
                        "childAge", 7
                ))
                .when()
                .post("/api/bookings")
                .then()
                .statusCode(400)
                .body("message", containsString("déjà une réservation"));
    }

    @Test
    @Order(9)
    @DisplayName("US-08: Given parent When GET bookings Then list")
    void us08_listBookings() {
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

        assertThat(bookings).isNotEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("US-10: Given admin/parent When dashboard Then 200/403")
    void us10_adminAccess() {
        adminToken = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "admin@kidsactivities.fr",
                        "password", "admin123"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(403);

        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(11)
    @DisplayName("US-11: Given admin When dashboard Then stats")
    void us11_adminDashboard() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200)
                .body("totalUsers", greaterThan(0))
                .body("totalActivities", greaterThan(0))
                .body("recentBookings", notNullValue());
    }

    @Test
    @Order(12)
    @DisplayName("US-12: Given admin When create activity Then availableSpots=maxCapacity")
    void us12_createActivity() {
        createdActivityId = given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "E2E Atelier",
                        "description", "Créé par test d'acceptation",
                        "startDateTime", LocalDateTime.now().plusDays(30).withMinute(0).withSecond(0).withNano(0).toString(),
                        "location", "Lyon",
                        "maxCapacity", 5,
                        "price", 20.00,
                        "active", true
                ))
                .when()
                .post("/api/activities")
                .then()
                .statusCode(201)
                .body("availableSpots", equalTo(5))
                .body("maxCapacity", equalTo(5))
                .extract()
                .path("id");
    }

    @Test
    @Order(13)
    @DisplayName("US-13: Given admin When update activity Then persisted")
    void us13_updateActivity() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "E2E Atelier Modifié",
                        "description", "Description mise à jour",
                        "startDateTime", LocalDateTime.now().plusDays(30).withMinute(0).withSecond(0).withNano(0).toString(),
                        "location", "Lyon",
                        "maxCapacity", 5,
                        "price", 25.00,
                        "active", true
                ))
                .when()
                .put("/api/activities/{id}", createdActivityId)
                .then()
                .statusCode(200)
                .body("title", equalTo("E2E Atelier Modifié"));
    }

    @Test
    @Order(14)
    @DisplayName("US-09: Given booking When cancel Then CANCELLED")
    void us09_cancelBooking() {
        given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + parentToken)
                .when()
                .delete("/api/bookings/{id}", bookingId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }

    @Test
    @Order(15)
    @DisplayName("US-21: Given full flow When completed Then state consistent")
    void us21_endToEndFlow() {
        assertThat(parentToken).isNotBlank();
        assertThat(adminToken).isNotBlank();
        assertThat(activityId).isNotNull();
        assertThat(bookingId).isNotNull();
    }
}
