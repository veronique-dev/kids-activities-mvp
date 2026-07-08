package com.kidsactivities.activity.dto;

import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.service.ActivityService;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private String title;
    private String description;
    private String details;
    private String prerequisites;
    private LocalDateTime startDateTime;
    private String location;
    private Integer maxCapacity;
    private Integer availableSpots;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;
    private Long catalogId;
    private String catalogName;
    private String catalogEmoji;
    private LocalDateTime registrationDeadline;
    private boolean bookingOpen;

    public static ActivityResponse from(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .details(activity.getDetails())
                .prerequisites(activity.getPrerequisites())
                .startDateTime(activity.getStartDateTime())
                .location(activity.getLocation())
                .maxCapacity(activity.getMaxCapacity())
                .availableSpots(activity.getAvailableSpots())
                .price(activity.getPrice())
                .active(activity.isActive())
                .createdAt(activity.getCreatedAt())
                .catalogId(activity.getCatalog() != null ? activity.getCatalog().getId() : null)
                .catalogName(activity.getCatalog() != null ? activity.getCatalog().getName() : null)
                .catalogEmoji(activity.getCatalog() != null ? activity.getCatalog().getEmoji() : null)
                .registrationDeadline(activity.getRegistrationDeadline())
                .bookingOpen(ActivityService.isBookingOpen(activity))
                .build();
    }
}
