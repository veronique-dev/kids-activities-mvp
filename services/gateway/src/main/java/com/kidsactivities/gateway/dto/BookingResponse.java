package com.kidsactivities.gateway.dto;

import com.kidsactivities.common.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private LocalDateTime activityStartDateTime;
    private String childName;
    private Integer childAge;
    private BookingStatus status;
    private Long userId;
    private String userEmail;
    private LocalDateTime createdAt;
}
