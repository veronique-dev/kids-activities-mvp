package com.kidsactivities.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent {
    private Long bookingId;
    private String userEmail;
    private String firstName;
    private String childName;
    private int childAge;
    private String activityTitle;
    private LocalDateTime startDateTime;
    private String location;
    private BigDecimal price;
}
