package com.kidsactivities.booking.controller;

import com.kidsactivities.booking.dto.request.BookingRequest;
import com.kidsactivities.booking.dto.response.BookingResponse;
import com.kidsactivities.booking.security.AuthenticatedUser;
import com.kidsactivities.booking.service.BookingService;
import com.kidsactivities.common.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Gestion des réservations")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Mes réservations ou toutes (admin)")
    public List<BookingResponse> getBookings(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user.getRole() == Role.ADMIN) {
            return bookingService.getAllBookings();
        }
        return bookingService.getUserBookings(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Réserver une activité")
    public BookingResponse createBooking(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody BookingRequest request
    ) {
        return bookingService.createBooking(user.getId(), request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation")
    public BookingResponse cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        return bookingService.cancelBooking(id, user.getId(), isAdmin);
    }
}
