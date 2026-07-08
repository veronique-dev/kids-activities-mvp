package com.kidsactivities.service;

import com.kidsactivities.dto.request.BookingRequest;
import com.kidsactivities.dto.response.BookingResponse;
import com.kidsactivities.entity.*;
import com.kidsactivities.exception.BadRequestException;
import com.kidsactivities.exception.ResourceNotFoundException;
import com.kidsactivities.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ActivityService activityService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldDecreaseAvailableSpots() {
        User user = buildUser();
        Activity activity = buildActivity();
        BookingRequest request = new BookingRequest();
        request.setActivityId(1L);
        request.setChildName("Lucas");
        request.setChildAge(8);

        when(activityService.findActivity(1L)).thenReturn(activity);
        when(bookingRepository.existsByUserIdAndActivityIdAndStatus(1L, 1L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(user, request);

        assertThat(response.getChildName()).isEqualTo("Lucas");
        assertThat(activity.getAvailableSpots()).isEqualTo(4);
    }

    @Test
    void createBooking_shouldThrowWhenNoSpotsLeft() {
        User user = buildUser();
        Activity activity = buildActivity();
        activity.setAvailableSpots(0);

        BookingRequest request = new BookingRequest();
        request.setActivityId(1L);
        request.setChildName("Lucas");
        request.setChildAge(8);

        when(activityService.findActivity(1L)).thenReturn(activity);

        assertThatThrownBy(() -> bookingService.createBooking(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Plus de places disponibles pour cette activité");
    }

    @Test
    void cancelBooking_shouldRestoreSpot() {
        User user = buildUser();
        Activity activity = buildActivity();
        activity.setAvailableSpots(4);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .activity(activity)
                .childName("Lucas")
                .childAge(8)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponse response = bookingService.cancelBooking(1L, user, false);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(activity.getAvailableSpots()).isEqualTo(5);
    }

    @Test
    void cancelBooking_shouldThrowWhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L, buildUser(), false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("parent@example.com")
                .firstName("Marie")
                .lastName("Dupont")
                .role(Role.PARENT)
                .build();
    }

    private Activity buildActivity() {
        return Activity.builder()
                .id(1L)
                .title("Atelier")
                .description("Desc")
                .startDateTime(LocalDateTime.now().plusDays(3))
                .location("Paris")
                .maxCapacity(5)
                .availableSpots(5)
                .price(new BigDecimal("20.00"))
                .active(true)
                .build();
    }
}
