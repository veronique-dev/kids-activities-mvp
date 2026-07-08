package com.kidsactivities.payment.service;

import com.kidsactivities.common.dto.InternalBookingSnapshot;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import com.kidsactivities.payment.security.InternalApiKeyFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class BookingServiceClient {

    private final RestClient bookingRestClient;

    public BookingServiceClient(@Qualifier("bookingRestClient") RestClient bookingRestClient) {
        this.bookingRestClient = bookingRestClient;
    }

    public InternalBookingSnapshot getBooking(Long bookingId) {
        try {
            return bookingRestClient.get()
                    .uri("/internal/bookings/{id}", bookingId)
                    .retrieve()
                    .body(InternalBookingSnapshot.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Réservation non trouvée");
        }
    }

    public void confirmBooking(Long bookingId) {
        bookingRestClient.post()
                .uri("/internal/bookings/{id}/confirm", bookingId)
                .retrieve()
                .toBodilessEntity();
    }
}
