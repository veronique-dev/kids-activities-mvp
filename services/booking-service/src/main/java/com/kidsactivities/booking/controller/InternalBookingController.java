package com.kidsactivities.booking.controller;

import com.kidsactivities.booking.dto.response.BookingResponse;
import com.kidsactivities.booking.dto.response.BookingStatsResponse;
import com.kidsactivities.booking.service.BookingService;
import com.kidsactivities.common.dto.InternalBookingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingService bookingService;

    @GetMapping("/stats")
    public BookingStatsResponse getStats() {
        return bookingService.getStats();
    }

    @GetMapping("/bookings/recent")
    public List<BookingResponse> getRecentBookings(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return bookingService.getRecentBookings(limit);
    }

    @GetMapping("/bookings/{id}")
    public InternalBookingSnapshot getBooking(@PathVariable Long id) {
        return bookingService.getBookingInternal(id);
    }

    @PostMapping("/bookings/{id}/confirm")
    public BookingResponse confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }
}
