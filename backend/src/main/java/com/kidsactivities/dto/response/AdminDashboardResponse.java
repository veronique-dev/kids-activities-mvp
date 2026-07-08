package com.kidsactivities.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalActivities;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private List<BookingResponse> recentBookings;
}
