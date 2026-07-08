package com.kidsactivities.activity.service;

import com.kidsactivities.activity.dto.ActivityRequest;
import com.kidsactivities.activity.dto.ActivityResponse;
import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.repository.ActivityRepository;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("US-01 / US-02 / US-12 / US-13 — ActivityService")
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    @DisplayName("US-12: Given valid request When create Then availableSpots = maxCapacity")
    void createActivity_shouldInitializeAvailableSpots() {
        ActivityRequest request = buildRequest(10);

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(1L);
            return activity;
        });

        ActivityResponse response = activityService.createActivity(request);

        assertThat(response.getAvailableSpots()).isEqualTo(10);
        assertThat(response.getMaxCapacity()).isEqualTo(10);
    }

    @Test
    @DisplayName("US-02: Given unknown id When getById Then 404")
    void getActivityById_shouldThrowWhenNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.getActivityById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("US-13: Given capacity < booked When update Then 400")
    void updateActivity_shouldRejectCapacityBelowBookedSpots() {
        Activity activity = buildActivity(10, 5);
        ActivityRequest request = buildRequest(3);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> activityService.updateActivity(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("capacité");
    }

    @Test
    @DisplayName("US-07: Given spots available When reserve Then decrements")
    void reserveSpot_shouldDecrementAvailableSpots() {
        Activity activity = buildActivity(5, 5);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);

        Activity result = activityService.reserveSpot(1L);

        assertThat(result.getAvailableSpots()).isEqualTo(4);
    }

    @Test
    @DisplayName("US-07: Given no spots When reserve Then 409")
    void reserveSpot_shouldRejectWhenFull() {
        Activity activity = buildActivity(5, 0);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> activityService.reserveSpot(1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("US-07: Given inactive activity When reserve Then 400")
    void reserveSpot_shouldRejectWhenInactive() {
        Activity activity = buildActivity(5, 5);
        activity.setActive(false);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> activityService.reserveSpot(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cette activité n'est plus disponible");
    }

    @Test
    @DisplayName("US-09: Given cancelled booking When release Then increments spots")
    void releaseSpot_shouldIncrementAvailableSpots() {
        Activity activity = buildActivity(5, 4);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);

        Activity result = activityService.releaseSpot(1L);

        assertThat(result.getAvailableSpots()).isEqualTo(5);
    }

    private ActivityRequest buildRequest(int maxCapacity) {
        ActivityRequest request = new ActivityRequest();
        request.setTitle("Atelier");
        request.setDescription("Description");
        request.setStartDateTime(LocalDateTime.now().plusDays(5));
        request.setLocation("Paris");
        request.setMaxCapacity(maxCapacity);
        request.setPrice(new BigDecimal("25.00"));
        request.setActive(true);
        return request;
    }

    private Activity buildActivity(int maxCapacity, int availableSpots) {
        return Activity.builder()
                .id(1L)
                .title("Atelier")
                .description("Desc")
                .startDateTime(LocalDateTime.now().plusDays(3))
                .location("Paris")
                .maxCapacity(maxCapacity)
                .availableSpots(availableSpots)
                .price(new BigDecimal("20.00"))
                .active(true)
                .build();
    }
}
