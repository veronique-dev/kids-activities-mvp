package com.kidsactivities.gateway.dto;

import lombok.Data;

@Data
public class BookingStatsResponse {
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
}
