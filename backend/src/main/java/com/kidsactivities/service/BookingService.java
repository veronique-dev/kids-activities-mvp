package com.kidsactivities.service;

import com.kidsactivities.dto.request.BookingRequest;
import com.kidsactivities.dto.response.BookingResponse;
import com.kidsactivities.entity.Activity;
import com.kidsactivities.entity.Booking;
import com.kidsactivities.entity.BookingStatus;
import com.kidsactivities.entity.User;
import com.kidsactivities.exception.BadRequestException;
import com.kidsactivities.exception.ResourceNotFoundException;
import com.kidsactivities.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

    public List<BookingResponse> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(User user, BookingRequest request) {
        Activity activity = activityService.findActivity(request.getActivityId());

        if (bookingRepository.existsByUserIdAndActivityIdAndStatus(
                user.getId(), activity.getId(), BookingStatus.CONFIRMED)) {
            throw new BadRequestException("Vous avez déjà une réservation confirmée pour cette activité");
        }

        if (!activity.isActive()) {
            throw new BadRequestException("Cette activité n'est plus disponible");
        }

        if (activity.getAvailableSpots() <= 0) {
            throw new BadRequestException("Plus de places disponibles pour cette activité");
        }

        Booking booking = Booking.builder()
                .user(user)
                .activity(activity)
                .childName(request.getChildName())
                .childAge(request.getChildAge())
                .status(BookingStatus.CONFIRMED)
                .build();

        activity.setAvailableSpots(activity.getAvailableSpots() - 1);
        Booking saved = bookingRepository.save(booking);

        emailService.sendBookingConfirmation(
                user.getEmail(),
                user.getFirstName(),
                saved.getChildName(),
                saved.getChildAge(),
                activity.getTitle(),
                activity.getStartDateTime(),
                activity.getLocation(),
                activity.getPrice());
        emailService.sendAdminBookingNotification(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                saved.getChildName(),
                saved.getChildAge(),
                activity.getTitle(),
                activity.getStartDateTime());

        return BookingResponse.from(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, User user, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (!isAdmin && !booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Vous ne pouvez annuler que vos propres réservations");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cette réservation est déjà annulée");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Activity activity = booking.getActivity();
        activity.setAvailableSpots(activity.getAvailableSpots() + 1);

        Booking saved = bookingRepository.save(booking);
        User parent = saved.getUser();

        emailService.sendBookingCancellation(
                parent.getEmail(),
                parent.getFirstName(),
                saved.getChildName(),
                activity.getTitle(),
                activity.getStartDateTime());

        return BookingResponse.from(saved);
    }
}
