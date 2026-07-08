package com.kidsactivities.booking.service;

import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class AuthServiceClient {

    private final RestClient authRestClient;

    public AuthServiceClient(@Qualifier("authRestClient") RestClient authRestClient) {
        this.authRestClient = authRestClient;
    }

    public UserSnapshot getUser(Long id) {
        try {
            return authRestClient.get()
                    .uri("/internal/users/{id}", id)
                    .retrieve()
                    .body(UserSnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Utilisateur non trouvé");
        }
    }
}
