package com.kidsactivities.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

    private boolean enabled = true;
    private String from = "noreply@kidsactivities.fr";
    private String admin = "admin@kidsactivities.fr";
}
