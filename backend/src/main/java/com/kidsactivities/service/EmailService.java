package com.kidsactivities.service;

import com.kidsactivities.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Bienvenue sur Kids Activities";
        String body = """
                Bonjour %s,

                Bienvenue sur Kids Activities ! Votre compte a bien été créé.

                Vous pouvez dès maintenant parcourir les activités et réserver une place pour vos enfants.

                À bientôt,
                L'équipe Kids Activities
                """.formatted(firstName);

        send(to, subject, body);
    }

    @Async
    public void sendBookingConfirmation(
            String to,
            String firstName,
            String childName,
            int childAge,
            String activityTitle,
            LocalDateTime startDateTime,
            String location,
            BigDecimal price) {
        String subject = "Confirmation de réservation — " + activityTitle;
        String body = """
                Bonjour %s,

                Votre réservation est confirmée.

                Activité : %s
                Date : %s
                Lieu : %s
                Prix : %s €
                Enfant : %s (%d ans)

                Vous pouvez consulter vos réservations depuis votre espace personnel.

                À bientôt,
                L'équipe Kids Activities
                """.formatted(
                firstName,
                activityTitle,
                formatDate(startDateTime),
                location,
                price.toPlainString(),
                childName,
                childAge);

        send(to, subject, body);
    }

    @Async
    public void sendBookingCancellation(
            String to,
            String firstName,
            String childName,
            String activityTitle,
            LocalDateTime startDateTime) {
        String subject = "Annulation de réservation — " + activityTitle;
        String body = """
                Bonjour %s,

                Votre réservation a bien été annulée.

                Activité : %s
                Date : %s
                Enfant : %s

                La place a été libérée et peut à nouveau être réservée.

                À bientôt,
                L'équipe Kids Activities
                """.formatted(
                firstName,
                activityTitle,
                formatDate(startDateTime),
                childName);

        send(to, subject, body);
    }

    @Async
    public void sendAdminBookingNotification(
            String parentEmail,
            String parentName,
            String childName,
            int childAge,
            String activityTitle,
            LocalDateTime startDateTime) {
        String subject = "Nouvelle réservation — " + activityTitle;
        String body = """
                Une nouvelle réservation vient d'être effectuée.

                Activité : %s
                Date : %s
                Parent : %s (%s)
                Enfant : %s (%d ans)
                """.formatted(
                activityTitle,
                formatDate(startDateTime),
                parentName,
                parentEmail,
                childName,
                childAge);

        send(emailProperties.getAdmin(), subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!emailProperties.isEnabled()) {
            log.debug("Emails désactivés, envoi ignoré : {}", subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email envoyé à {} : {}", to, subject);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email à {} ({})", to, subject, e);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMAT);
    }
}
