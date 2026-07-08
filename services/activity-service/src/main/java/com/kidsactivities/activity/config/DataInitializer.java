package com.kidsactivities.activity.config;

import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ActivityRepository activityRepository;

    @Override
    public void run(String... args) {
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
