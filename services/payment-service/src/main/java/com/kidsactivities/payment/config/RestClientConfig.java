package com.kidsactivities.payment.config;

import com.kidsactivities.payment.security.InternalApiKeyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient bookingRestClient(
            @Value("${app.booking-service-url}") String baseUrl,
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(InternalApiKeyFilter.INTERNAL_KEY_HEADER, internalApiKey)
                .build();
    }
}
