package com.kidsactivities.booking.service;

import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class ActivityServiceClient {

    private final RestClient activityRestClient;

    public ActivityServiceClient(@Qualifier("activityRestClient") RestClient activityRestClient) {
        this.activityRestClient = activityRestClient;
    }

    public ActivitySnapshot getActivity(Long id) {
        try {
            return activityRestClient.get()
                    .uri("/internal/activities/{id}", id)
                    .retrieve()
                    .body(ActivitySnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Activité non trouvée");
        }
    }

    public ActivitySnapshot reserveSpot(Long id) {
        try {
            return activityRestClient.post()
                    .uri("/internal/activities/{id}/reserve", id)
                    .retrieve()
                    .body(ActivitySnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Activité non trouvée");
        } catch (HttpClientErrorException.Conflict e) {
            throw new BadRequestException("Plus de places disponibles pour cette activité");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new BadRequestException("Cette activité n'est plus disponible");
        }
    }

    public ActivitySnapshot releaseSpot(Long id) {
        try {
            return activityRestClient.post()
                    .uri("/internal/activities/{id}/release", id)
                    .retrieve()
                    .body(ActivitySnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Activité non trouvée");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException("Impossible de libérer une place pour cette activité");
            }
            throw e;
        }
    }
}
