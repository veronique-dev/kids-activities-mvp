package com.kidsactivities.notification.service;

import com.kidsactivities.notification.config.EmailProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("US-17 / US-18 / US-19 — EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailProperties emailProperties;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("US-17: Given enabled When welcome Then email sent")
    void sendWelcomeEmail_shouldSendWhenEnabled() {
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@kidsactivities.fr");

        emailService.sendWelcomeEmail("parent@example.com", "Marie");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()[0]).isEqualTo("parent@example.com");
        assertThat(message.getSubject()).contains("Bienvenue");
        assertThat(message.getText()).contains("Marie");
    }

    @Test
    @DisplayName("US-18: Given enabled When booking confirmed Then email sent")
    void sendBookingConfirmation_shouldSendWhenEnabled() {
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@kidsactivities.fr");

        emailService.sendBookingConfirmation(
                "parent@example.com",
                "Marie",
                "Lucas",
                8,
                "Atelier peinture",
                LocalDateTime.of(2026, 12, 1, 14, 0),
                "Paris",
                new BigDecimal("25.00")
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).contains("Confirmation");
        assertThat(captor.getValue().getText()).contains("Lucas");
    }

    @Test
    @DisplayName("US-19: Given enabled When cancellation Then email sent")
    void sendBookingCancellation_shouldSendWhenEnabled() {
        when(emailProperties.isEnabled()).thenReturn(true);
        when(emailProperties.getFrom()).thenReturn("noreply@kidsactivities.fr");

        emailService.sendBookingCancellation(
                "parent@example.com",
                "Marie",
                "Lucas",
                "Atelier peinture",
                LocalDateTime.of(2026, 12, 1, 14, 0)
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).contains("Annulation");
    }

    @Test
    @DisplayName("US-17: Given disabled When welcome Then no email")
    void sendWelcomeEmail_shouldSkipWhenDisabled() {
        when(emailProperties.isEnabled()).thenReturn(false);

        emailService.sendWelcomeEmail("parent@example.com", "Marie");

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
