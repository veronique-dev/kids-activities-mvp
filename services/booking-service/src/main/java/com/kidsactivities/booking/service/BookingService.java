package com.kidsactivities.booking.service;

import com.kidsactivities.booking.dto.request.BookingRequest;
import com.kidsactivities.booking.dto.response.BookingResponse;
import com.kidsactivities.booking.dto.response.BookingStatsResponse;
import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.booking.repository.BookingRepository;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import com.kidsactivities.common.model.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ActivityServiceClient activityServiceClient;
    private final AuthServiceClient authServiceClient;
    private final BookingEventPublisher bookingEventPublisher;

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    public List<BookingResponse> getRecentBookings(int limit) {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .map(this::toEnrichedResponse)
                .toList();
    }

    public BookingStatsResponse getStats() {
        return BookingStatsResponse.builder()
                .totalBookings(bookingRepository.count())
                .confirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED))
                .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                .build();
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        if (bookingRepository.existsByUserIdAndActivityIdAndStatus(
                userId, request.getActivityId(), BookingStatus.CONFIRMED)) {
            throw new BadRequestException("Vous avez déjà une réservation confirmée pour cette activité");
        }

        ActivitySnapshot activity = activityServiceClient.reserveSpot(request.getActivityId());

        try {
            Booking booking = Booking.builder()
                    .userId(userId)
                    .activityId(request.getActivityId())
                    .childName(request.getChildName())
                    .childAge(request.getChildAge())
                    .status(BookingStatus.CONFIRMED)
                    .build();

            Booking saved = bookingRepository.save(booking);
            UserSnapshot user = authServiceClient.getUser(userId);

            bookingEventPublisher.publishBookingConfirmed(saved, activity, user);

            return BookingResponse.from(saved, activity, user);
        } catch (RuntimeException ex) {
            activityServiceClient.releaseSpot(request.getActivityId());
            throw ex;
        }
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new BadRequestException("Vous ne pouvez annuler que vos propres réservations");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cette réservation est déjà annulée");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        ActivitySnapshot activity = activityServiceClient.releaseSpot(saved.getActivityId());
        UserSnapshot user = authServiceClient.getUser(saved.getUserId());

        bookingEventPublisher.publishBookingCancelled(saved, activity, user);

        return BookingResponse.from(saved, activity, user);
    }

    private BookingResponse toEnrichedResponse(Booking booking) {
        ActivitySnapshot activity = activityServiceClient.getActivity(booking.getActivityId());
        UserSnapshot user = authServiceClient.getUser(booking.getUserId());
        return BookingResponse.from(booking, activity, user);
    }
}
