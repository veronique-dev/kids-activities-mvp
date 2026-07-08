package com.kidsactivities.service;

import com.kidsactivities.dto.request.ActivityRequest;
import com.kidsactivities.dto.response.ActivityResponse;
import com.kidsactivities.entity.Activity;
import com.kidsactivities.exception.BadRequestException;
import com.kidsactivities.exception.ResourceNotFoundException;
import com.kidsactivities.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void createActivity_shouldReturnCreatedActivity() {
        ActivityRequest request = buildRequest();

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(1L);
            return activity;
        });

        ActivityResponse response = activityService.createActivity(request);

        assertThat(response.getTitle()).isEqualTo("Atelier peinture");
        assertThat(response.getAvailableSpots()).isEqualTo(10);
    }

    @Test
    void getActivityById_shouldThrowWhenNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.getActivityById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateActivity_shouldRejectCapacityBelowBookedSpots() {
        Activity activity = Activity.builder()
                .id(1L)
                .title("Test")
                .description("Desc")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .location("Paris")
                .maxCapacity(10)
                .availableSpots(5)
                .price(BigDecimal.TEN)
                .active(true)
                .build();

        ActivityRequest request = buildRequest();
        request.setMaxCapacity(3);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> activityService.updateActivity(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    private ActivityRequest buildRequest() {
        ActivityRequest request = new ActivityRequest();
        request.setTitle("Atelier peinture");
        request.setDescription("Description");
        request.setStartDateTime(LocalDateTime.now().plusDays(5));
        request.setLocation("Paris");
        request.setMaxCapacity(10);
        request.setPrice(new BigDecimal("25.00"));
        request.setActive(true);
        return request;
    }
}
