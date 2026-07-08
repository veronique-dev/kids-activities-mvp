package com.kidsactivities.gateway.service;

import com.kidsactivities.gateway.dto.AdminDashboardResponse;
import com.kidsactivities.gateway.dto.BookingResponse;
import com.kidsactivities.gateway.dto.BookingStatsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AdminDashboardAggregator {

    private final WebClient authWebClient;
    private final WebClient activityWebClient;
    private final WebClient bookingWebClient;

    public AdminDashboardAggregator(
            @Qualifier("authWebClient") WebClient authWebClient,
            @Qualifier("activityWebClient") WebClient activityWebClient,
            @Qualifier("bookingWebClient") WebClient bookingWebClient) {
        this.authWebClient = authWebClient;
        this.activityWebClient = activityWebClient;
        this.bookingWebClient = bookingWebClient;
    }

    public Mono<AdminDashboardResponse> getDashboard() {
        Mono<Long> totalUsers = authWebClient.get()
                .uri("/internal/stats/count")
                .retrieve()
                .bodyToMono(Long.class);

        Mono<Long> totalActivities = activityWebClient.get()
                .uri("/internal/stats/count")
                .retrieve()
                .bodyToMono(Long.class);

        Mono<BookingStatsResponse> bookingStats = bookingWebClient.get()
                .uri("/internal/stats")
                .retrieve()
                .bodyToMono(BookingStatsResponse.class);

        Mono<List<BookingResponse>> recentBookings = bookingWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/bookings/recent")
                        .queryParam("limit", 10)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BookingResponse>>() {});

        return Mono.zip(totalUsers, totalActivities, bookingStats, recentBookings)
                .map(tuple -> AdminDashboardResponse.builder()
                        .totalUsers(tuple.getT1())
                        .totalActivities(tuple.getT2())
                        .totalBookings(tuple.getT3().getTotalBookings())
                        .confirmedBookings(tuple.getT3().getConfirmedBookings())
                        .cancelledBookings(tuple.getT3().getCancelledBookings())
                        .recentBookings(tuple.getT4())
                        .build());
    }
}
