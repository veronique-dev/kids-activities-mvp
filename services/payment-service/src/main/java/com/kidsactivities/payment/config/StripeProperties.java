package com.kidsactivities.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.stripe")
public class StripeProperties {

    private boolean enabled = false;
    private String secretKey = "";
    private String webhookSecret = "";
    private String frontendUrl = "http://localhost:5173";

    public boolean isConfigured() {
        return enabled && secretKey != null && !secretKey.isBlank();
    }
}
