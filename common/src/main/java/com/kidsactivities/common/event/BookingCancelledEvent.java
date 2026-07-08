package com.kidsactivities.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
    private Long bookingId;
    private String userEmail;
    private String firstName;
    private String childName;
    private String activityTitle;
    private LocalDateTime startDateTime;
}
