package com.kidsactivities.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String INTERNAL_KEY_HEADER = "X-Internal-Key";

    @Bean
    @Qualifier("authWebClient")
    public WebClient authWebClient(
            @Value("${app.services.auth}") String baseUrl,
            @Value("${app.internal-api-key}") String internalApiKey) {
        return buildInternalWebClient(baseUrl, internalApiKey);
    }

    @Bean
    @Qualifier("activityWebClient")
    public WebClient activityWebClient(
            @Value("${app.services.activity}") String baseUrl,
            @Value("${app.internal-api-key}") String internalApiKey) {
        return buildInternalWebClient(baseUrl, internalApiKey);
    }

    @Bean
    @Qualifier("bookingWebClient")
    public WebClient bookingWebClient(
            @Value("${app.services.booking}") String baseUrl,
            @Value("${app.internal-api-key}") String internalApiKey) {
        return buildInternalWebClient(baseUrl, internalApiKey);
    }

    private WebClient buildInternalWebClient(String baseUrl, String internalApiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(INTERNAL_KEY_HEADER, internalApiKey)
                .build();
    }
}
