package com.kidsactivities.controller;

import com.kidsactivities.dto.request.BookingRequest;
import com.kidsactivities.dto.response.BookingResponse;
import com.kidsactivities.entity.Role;
import com.kidsactivities.entity.User;
import com.kidsactivities.service.BookingService;
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
    public List<BookingResponse> getBookings(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.ADMIN) {
            return bookingService.getAllBookings();
        }
        return bookingService.getUserBookings(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Réserver une activité")
    public BookingResponse createBooking(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookingRequest request
    ) {
        return bookingService.createBooking(user, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation")
    public BookingResponse cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        return bookingService.cancelBooking(id, user, isAdmin);
    }
}
