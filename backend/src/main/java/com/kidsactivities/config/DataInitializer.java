package com.kidsactivities.config;

import com.kidsactivities.entity.Activity;
import com.kidsactivities.entity.Role;
import com.kidsactivities.entity.User;
import com.kidsactivities.repository.ActivityRepository;
import com.kidsactivities.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("admin@kidsactivities.fr")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("System")
                    .role(Role.ADMIN)
                    .build());

            userRepository.save(User.builder()
                    .email("parent@example.com")
                    .password(passwordEncoder.encode("parent123"))
                    .firstName("Marie")
                    .lastName("Dupont")
                    .role(Role.PARENT)
                    .build());
        }

        if (activityRepository.count() == 0) {
            activityRepository.save(Activity.builder()
                    .title("Atelier peinture")
                    .description("Découverte de la peinture acrylique pour les 6-10 ans.")
                    .startDateTime(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0))
                    .location("Maison des associations, Paris 11e")
                    .maxCapacity(12)
                    .availableSpots(12)
                    .price(new BigDecimal("25.00"))
                    .active(true)
                    .build());

            activityRepository.save(Activity.builder()
                    .title("Stage théâtre vacances")
                    .description("Une semaine de théâtre et improvisation pour les 8-14 ans.")
                    .startDateTime(LocalDateTime.now().plusDays(14).withHour(9).withMinute(0))
                    .location("Théâtre municipal, Lyon")
                    .maxCapacity(20)
                    .availableSpots(20)
                    .price(new BigDecimal("120.00"))
                    .active(true)
                    .build());

            activityRepository.save(Activity.builder()
                    .title("Cours de natation débutant")
                    .description("Apprentissage des bases de la natation en petit groupe.")
                    .startDateTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(30))
                    .location("Piscine Olympique, Marseille")
                    .maxCapacity(8)
                    .availableSpots(8)
                    .price(new BigDecimal("18.50"))
                    .active(true)
                    .build());
        }
    }
}
