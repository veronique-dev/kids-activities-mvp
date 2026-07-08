package com.kidsactivities.booking.service;

import com.kidsactivities.booking.dto.request.BookingRequest;
import com.kidsactivities.booking.dto.response.BookingResponse;
import com.kidsactivities.booking.dto.response.BookingStatsResponse;
import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.booking.repository.BookingRepository;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.InternalBookingSnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import com.kidsactivities.common.model.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.CONFIRMED,
            BookingStatus.PENDING_PAYMENT
    );

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
                .pendingPaymentBookings(bookingRepository.countByStatus(BookingStatus.PENDING_PAYMENT))
                .build();
    }

    public InternalBookingSnapshot getBookingInternal(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));
        return toInternalSnapshot(booking);
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        if (bookingRepository.existsByUserIdAndActivityIdAndStatusIn(
                userId, request.getActivityId(), ACTIVE_BOOKING_STATUSES)) {
            throw new BadRequestException("Vous avez déjà une réservation en cours pour cette activité");
        }

        ActivitySnapshot activity = activityServiceClient.getActivity(request.getActivityId());

        assertBookingOpen(activity);

        if (isFreeActivity(activity)) {
            return confirmPaidBooking(userId, request, activity);
        }

        Booking booking = Booking.builder()
                .userId(userId)
                .activityId(request.getActivityId())
                .childName(request.getChildName())
                .childAge(request.getChildAge())
                .status(BookingStatus.PENDING_PAYMENT)
                .amount(activity.getPrice())
                .currency("EUR")
                .build();

        Booking saved = bookingRepository.save(booking);
        UserSnapshot user = authServiceClient.getUser(userId);
        return BookingResponse.from(saved, activity, user);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return toEnrichedResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Cette réservation ne peut pas être confirmée");
        }

        ActivitySnapshot activity = activityServiceClient.reserveSpot(booking.getActivityId());

        try {
            booking.setStatus(BookingStatus.CONFIRMED);
            Booking saved = bookingRepository.save(booking);
            UserSnapshot user = authServiceClient.getUser(saved.getUserId());
            bookingEventPublisher.publishBookingConfirmed(saved, activity, user);
            return BookingResponse.from(saved, activity, user);
        } catch (RuntimeException ex) {
            activityServiceClient.releaseSpot(booking.getActivityId());
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

        boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        ActivitySnapshot activity = wasConfirmed
                ? activityServiceClient.releaseSpot(saved.getActivityId())
                : activityServiceClient.getActivity(saved.getActivityId());
        UserSnapshot user = authServiceClient.getUser(saved.getUserId());

        if (wasConfirmed) {
            bookingEventPublisher.publishBookingCancelled(saved, activity, user);
        }

        return BookingResponse.from(saved, activity, user);
    }

    private BookingResponse confirmPaidBooking(Long userId, BookingRequest request, ActivitySnapshot activity) {
        ActivitySnapshot reservedActivity = activityServiceClient.reserveSpot(request.getActivityId());

        try {
            Booking booking = Booking.builder()
                    .userId(userId)
                    .activityId(request.getActivityId())
                    .childName(request.getChildName())
                    .childAge(request.getChildAge())
                    .status(BookingStatus.CONFIRMED)
                    .amount(BigDecimal.ZERO)
                    .currency("EUR")
                    .build();

            Booking saved = bookingRepository.save(booking);
            UserSnapshot user = authServiceClient.getUser(userId);
            bookingEventPublisher.publishBookingConfirmed(saved, reservedActivity, user);
            return BookingResponse.from(saved, reservedActivity, user);
        } catch (RuntimeException ex) {
            activityServiceClient.releaseSpot(request.getActivityId());
            throw ex;
        }
    }

    private boolean isFreeActivity(ActivitySnapshot activity) {
        return activity.getPrice() == null || activity.getPrice().compareTo(BigDecimal.ZERO) <= 0;
    }

    private void assertBookingOpen(ActivitySnapshot activity) {
        if (activity.isBookingOpen()) {
            return;
        }
        if (activity.getRegistrationDeadline() != null
                && !LocalDateTime.now().isBefore(activity.getRegistrationDeadline())) {
            throw new BadRequestException("Les inscriptions sont closes pour cette activité");
        }
        throw new BadRequestException("Cette activité n'est plus disponible à la réservation");
    }

    private BookingResponse toEnrichedResponse(Booking booking) {
        ActivitySnapshot activity = activityServiceClient.getActivity(booking.getActivityId());
        UserSnapshot user = authServiceClient.getUser(booking.getUserId());
        return BookingResponse.from(booking, activity, user);
    }

    private InternalBookingSnapshot toInternalSnapshot(Booking booking) {
        return InternalBookingSnapshot.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .activityId(booking.getActivityId())
                .status(booking.getStatus())
                .amount(booking.getAmount())
                .currency(booking.getCurrency())
                .childName(booking.getChildName())
                .build();
    }
}
