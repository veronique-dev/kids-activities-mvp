package com.kidsactivities.booking.dto.response;

import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
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

    public static BookingResponse from(Booking booking, ActivitySnapshot activity, UserSnapshot user) {
        return BookingResponse.builder()
                .id(booking.getId())
                .activityId(booking.getActivityId())
                .activityTitle(activity.getTitle())
                .activityStartDateTime(activity.getStartDateTime())
                .childName(booking.getChildName())
                .childAge(booking.getChildAge())
                .status(booking.getStatus())
                .userId(booking.getUserId())
                .userEmail(user.getEmail())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
