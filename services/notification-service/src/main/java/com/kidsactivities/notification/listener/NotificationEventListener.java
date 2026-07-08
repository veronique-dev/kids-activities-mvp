package com.kidsactivities.notification.listener;

import com.kidsactivities.common.event.BookingCancelledEvent;
import com.kidsactivities.common.event.BookingConfirmedEvent;
import com.kidsactivities.common.event.UserRegisteredEvent;
import com.kidsactivities.notification.config.RabbitMQConfig;
import com.kidsactivities.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Événement UserRegistered reçu pour {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName());
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CONFIRMED_QUEUE)
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Événement BookingConfirmed reçu pour la réservation {}", event.getBookingId());
        emailService.sendBookingConfirmation(
                event.getUserEmail(),
                event.getFirstName(),
                event.getChildName(),
                event.getChildAge(),
                event.getActivityTitle(),
                event.getStartDateTime(),
                event.getLocation(),
                event.getPrice());
        emailService.sendAdminBookingNotification(
                event.getUserEmail(),
                event.getFirstName(),
                event.getChildName(),
                event.getChildAge(),
                event.getActivityTitle(),
                event.getStartDateTime());
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CANCELLED_QUEUE)
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Événement BookingCancelled reçu pour la réservation {}", event.getBookingId());
        emailService.sendBookingCancellation(
                event.getUserEmail(),
                event.getFirstName(),
                event.getChildName(),
                event.getActivityTitle(),
                event.getStartDateTime());
    }
}
