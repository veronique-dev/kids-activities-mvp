package com.kidsactivities.booking.service;

import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.dto.UserSnapshot;
import com.kidsactivities.common.event.BookingCancelledEvent;
import com.kidsactivities.common.event.BookingConfirmedEvent;
import com.kidsactivities.common.event.RabbitConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookingConfirmed(Booking booking, ActivitySnapshot activity, UserSnapshot user) {
        rabbitTemplate.convertAndSend(
                RabbitConstants.EXCHANGE,
                RabbitConstants.BOOKING_CONFIRMED,
                BookingConfirmedEvent.builder()
                        .bookingId(booking.getId())
                        .userEmail(user.getEmail())
                        .firstName(user.getFirstName())
                        .childName(booking.getChildName())
                        .childAge(booking.getChildAge())
                        .activityTitle(activity.getTitle())
                        .startDateTime(activity.getStartDateTime())
                        .location(activity.getLocation())
                        .price(activity.getPrice())
                        .build()
        );
    }

    public void publishBookingCancelled(Booking booking, ActivitySnapshot activity, UserSnapshot user) {
        rabbitTemplate.convertAndSend(
                RabbitConstants.EXCHANGE,
                RabbitConstants.BOOKING_CANCELLED,
                BookingCancelledEvent.builder()
                        .bookingId(booking.getId())
                        .userEmail(user.getEmail())
                        .firstName(user.getFirstName())
                        .childName(booking.getChildName())
                        .activityTitle(activity.getTitle())
                        .startDateTime(activity.getStartDateTime())
                        .build()
        );
    }
}
