package com.kidsactivities.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Tag("e2e")
@DisplayName("US-17 / US-18 / US-19 — Notifications e-mail (Mailpit)")
@EnabledIfSystemProperty(named = "e2e.mailpit.enabled", matches = "true")
class NotificationsE2ETest {

    private static String baseUrl;
    private static String mailpitUrl;

    @BeforeAll
    static void setup() {
        baseUrl = System.getProperty("e2e.base.url", "http://localhost:8080");
        mailpitUrl = System.getProperty("e2e.mailpit.url", "http://localhost:8025");
        clearMailpitInbox();
    }

    @Test
    @DisplayName("US-17: Given register When success Then welcome email in Mailpit")
    void us17_welcomeEmailAfterRegistration() {
        String email = "qa-mail-" + UUID.randomUUID() + "@example.com";

        given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(Map.of(
                        "email", email,
                        "password", "password123",
                        "firstName", "Mail",
                        "lastName", "Test"
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(findMessagesTo(email))
                        .anyMatch(subject -> subject.contains("Bienvenue"))
        );
    }

    private static void clearMailpitInbox() {
        given()
                .baseUri(mailpitUrl)
                .when()
                .delete("/api/v1/messages")
                .then()
                .statusCode(200);
    }

    private static List<String> findMessagesTo(String email) {
        List<Map<String, Object>> messages = given()
                .baseUri(mailpitUrl)
                .when()
                .get("/api/v1/messages")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("messages");

        return messages.stream()
                .filter(msg -> messageSentTo(msg, email))
                .map(msg -> String.valueOf(msg.get("Subject")))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private static boolean messageSentTo(Map<String, Object> message, String email) {
        Object to = message.get("To");
        if (!(to instanceof List<?> recipients)) {
            return false;
        }
        return recipients.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .anyMatch(recipient -> email.equalsIgnoreCase(String.valueOf(recipient.get("Address"))));
    }
}
