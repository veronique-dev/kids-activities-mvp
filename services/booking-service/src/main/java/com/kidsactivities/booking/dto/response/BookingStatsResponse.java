package com.kidsactivities.booking.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingStatsResponse {
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
}
