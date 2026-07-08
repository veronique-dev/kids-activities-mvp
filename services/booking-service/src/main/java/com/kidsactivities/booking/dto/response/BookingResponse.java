package com.kidsactivities.booking.dto.response;

import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
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
    private BigDecimal amount;
    private String currency;
    private boolean paymentRequired;
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
                .amount(booking.getAmount())
                .currency(booking.getCurrency())
                .paymentRequired(booking.getStatus() == BookingStatus.PENDING_PAYMENT)
                .userId(booking.getUserId())
                .userEmail(user.getEmail())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
