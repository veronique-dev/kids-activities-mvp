package com.kidsactivities.dto.response;

import com.kidsactivities.entity.Activity;
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
    private LocalDateTime startDateTime;
    private String location;
    private Integer maxCapacity;
    private Integer availableSpots;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;

    public static ActivityResponse from(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .startDateTime(activity.getStartDateTime())
                .location(activity.getLocation())
                .maxCapacity(activity.getMaxCapacity())
                .availableSpots(activity.getAvailableSpots())
                .price(activity.getPrice())
                .active(activity.isActive())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
