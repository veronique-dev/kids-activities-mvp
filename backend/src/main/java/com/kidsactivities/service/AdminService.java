package com.kidsactivities.service;

import com.kidsactivities.dto.response.AdminDashboardResponse;
import com.kidsactivities.dto.response.BookingResponse;
import com.kidsactivities.entity.BookingStatus;
import com.kidsactivities.repository.ActivityRepository;
import com.kidsactivities.repository.BookingRepository;
import com.kidsactivities.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardResponse getDashboard() {
        List<BookingResponse> recentBookings = bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(10)
                .map(BookingResponse::from)
                .toList();

        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalActivities(activityRepository.count())
                .totalBookings(bookingRepository.count())
                .confirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED))
                .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                .recentBookings(recentBookings)
                .build();
    }
}
