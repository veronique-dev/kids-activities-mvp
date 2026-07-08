package com.kidsactivities.dto.response;

import com.kidsactivities.entity.Booking;
import com.kidsactivities.entity.BookingStatus;
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

    public static BookingResponse from(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .activityId(booking.getActivity().getId())
                .activityTitle(booking.getActivity().getTitle())
                .activityStartDateTime(booking.getActivity().getStartDateTime())
                .childName(booking.getChildName())
                .childAge(booking.getChildAge())
                .status(booking.getStatus())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
