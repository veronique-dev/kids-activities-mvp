package com.kidsactivities.booking.service;

import com.kidsactivities.booking.dto.request.BookingRequest;
import com.kidsactivities.booking.dto.response.BookingResponse;
import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.booking.repository.BookingRepository;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import com.kidsactivities.common.model.BookingStatus;
import com.kidsactivities.common.model.Role;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ActivityServiceClient activityServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldReserveSpotAndPublishEvent() {
        BookingRequest request = buildRequest();
        ActivitySnapshot activity = buildActivitySnapshot();
        UserSnapshot user = buildUserSnapshot();

        when(bookingRepository.existsByUserIdAndActivityIdAndStatus(1L, 1L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(activityServiceClient.reserveSpot(1L)).thenReturn(activity);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });
        when(authServiceClient.getUser(1L)).thenReturn(user);

        BookingResponse response = bookingService.createBooking(1L, request);

        assertThat(response.getChildName()).isEqualTo("Lucas");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingEventPublisher).publishBookingConfirmed(any(Booking.class), eq(activity), eq(user));
    }

    @Test
    void createBooking_shouldRejectDuplicateBeforeReserve() {
        BookingRequest request = buildRequest();

        when(bookingRepository.existsByUserIdAndActivityIdAndStatus(1L, 1L, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Vous avez déjà une réservation confirmée pour cette activité");

        verify(activityServiceClient, never()).reserveSpot(1L);
    }

    @Test
    void createBooking_shouldReleaseSpotWhenPersistenceFails() {
        BookingRequest request = buildRequest();
        ActivitySnapshot activity = buildActivitySnapshot();

        when(bookingRepository.existsByUserIdAndActivityIdAndStatus(1L, 1L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(activityServiceClient.reserveSpot(1L)).thenReturn(activity);
        when(bookingRepository.save(any(Booking.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(activityServiceClient).releaseSpot(1L);
    }

    @Test
    void createBooking_shouldMapNoSpotsConflictToBadRequest() {
        BookingRequest request = buildRequest();

        when(bookingRepository.existsByUserIdAndActivityIdAndStatus(1L, 1L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(activityServiceClient.reserveSpot(1L))
                .thenThrow(new BadRequestException("Plus de places disponibles pour cette activité"));

        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Plus de places disponibles pour cette activité");
    }

    @Test
    void cancelBooking_shouldReleaseSpotAndPublishEvent() {
        Booking booking = Booking.builder()
                .id(10L)
                .userId(1L)
                .activityId(1L)
                .childName("Lucas")
                .childAge(8)
                .status(BookingStatus.CONFIRMED)
                .build();
        ActivitySnapshot activity = buildActivitySnapshot();
        UserSnapshot user = buildUserSnapshot();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(activityServiceClient.releaseSpot(1L)).thenReturn(activity);
        when(authServiceClient.getUser(1L)).thenReturn(user);

        BookingResponse response = bookingService.cancelBooking(10L, 1L, false);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingEventPublisher).publishBookingCancelled(booking, activity, user);
    }

    @Test
    void cancelBooking_shouldRejectWhenNotOwner() {
        Booking booking = Booking.builder()
                .id(10L)
                .userId(2L)
                .activityId(1L)
                .childName("Lucas")
                .childAge(8)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, 1L, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Vous ne pouvez annuler que vos propres réservations");
    }

    @Test
    void cancelBooking_shouldThrowWhenNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L, 1L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private BookingRequest buildRequest() {
        BookingRequest request = new BookingRequest();
        request.setActivityId(1L);
        request.setChildName("Lucas");
        request.setChildAge(8);
        return request;
    }

    private ActivitySnapshot buildActivitySnapshot() {
        return ActivitySnapshot.builder()
                .id(1L)
                .title("Atelier peinture")
                .startDateTime(LocalDateTime.now().plusDays(3))
                .location("Paris")
                .price(new BigDecimal("25.00"))
                .active(true)
                .availableSpots(4)
                .build();
    }

    private UserSnapshot buildUserSnapshot() {
        return UserSnapshot.builder()
                .id(1L)
                .email("parent@example.com")
                .firstName("Marie")
                .lastName("Dupont")
                .role(Role.PARENT)
                .build();
    }
}
