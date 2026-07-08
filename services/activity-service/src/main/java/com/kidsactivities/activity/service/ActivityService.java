package com.kidsactivities.activity.service;

import com.kidsactivities.activity.dto.ActivityRequest;
import com.kidsactivities.activity.dto.ActivityResponse;
import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.entity.Catalog;
import com.kidsactivities.activity.repository.ActivityRepository;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CatalogService catalogService;

    public List<ActivityResponse> getActiveActivities(Long catalogId) {
        List<Activity> activities = catalogId == null
                ? activityRepository.findByActiveTrueOrderByStartDateTimeAsc()
                : activityRepository.findByActiveTrueAndCatalogIdOrderByStartDateTimeAsc(catalogId);

        return activities.stream()
                .map(ActivityResponse::from)
                .toList();
    }

    public List<ActivityResponse> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(ActivityResponse::from)
                .toList();
    }

    public ActivityResponse getActivityById(Long id) {
        Activity activity = findActivity(id);
        return ActivityResponse.from(activity);
    }

    public long countActivities() {
        return activityRepository.count();
    }

    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        validateActivityDates(request);
        Catalog catalog = catalogService.findCatalog(request.getCatalogId());

        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .details(request.getDetails())
                .prerequisites(request.getPrerequisites())
                .startDateTime(request.getStartDateTime())
                .location(request.getLocation())
                .maxCapacity(request.getMaxCapacity())
                .availableSpots(request.getMaxCapacity())
                .price(request.getPrice())
                .active(request.isActive())
                .catalog(catalog)
                .registrationDeadline(request.getRegistrationDeadline())
                .build();

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        validateActivityDates(request);
        Activity activity = findActivity(id);
        Catalog catalog = catalogService.findCatalog(request.getCatalogId());

        int bookedSpots = activity.getMaxCapacity() - activity.getAvailableSpots();
        int newMaxCapacity = request.getMaxCapacity();
        if (newMaxCapacity < bookedSpots) {
            throw new BadRequestException(
                    "La capacité ne peut pas être inférieure au nombre de places déjà réservées"
            );
        }

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setDetails(request.getDetails());
        activity.setPrerequisites(request.getPrerequisites());
        activity.setStartDateTime(request.getStartDateTime());
        activity.setLocation(request.getLocation());
        activity.setMaxCapacity(newMaxCapacity);
        activity.setAvailableSpots(newMaxCapacity - bookedSpots);
        activity.setPrice(request.getPrice());
        activity.setActive(request.isActive());
        activity.setCatalog(catalog);
        activity.setRegistrationDeadline(request.getRegistrationDeadline());

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activité non trouvée");
        }
        activityRepository.deleteById(id);
    }

    @Transactional
    public Activity reserveSpot(Long id) {
        Activity activity = findActivity(id);

        if (!activity.isActive()) {
            throw new BadRequestException("Cette activité n'est plus disponible");
        }

        if (activity.getRegistrationDeadline() != null
                && !LocalDateTime.now().isBefore(activity.getRegistrationDeadline())) {
            throw new BadRequestException("Les inscriptions sont closes pour cette activité");
        }

        if (activity.getAvailableSpots() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plus de places disponibles pour cette activité");
        }

        activity.setAvailableSpots(activity.getAvailableSpots() - 1);
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity releaseSpot(Long id) {
        Activity activity = findActivity(id);

        int newAvailableSpots = activity.getAvailableSpots() + 1;
        if (newAvailableSpots > activity.getMaxCapacity()) {
            throw new BadRequestException("Impossible de libérer une place : capacité maximale atteinte");
        }

        activity.setAvailableSpots(newAvailableSpots);
        return activityRepository.save(activity);
    }

    public Activity findActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activité non trouvée"));
    }

    public ActivitySnapshot toSnapshot(Activity activity) {
        return ActivitySnapshot.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .startDateTime(activity.getStartDateTime())
                .location(activity.getLocation())
                .price(activity.getPrice())
                .active(activity.isActive())
                .availableSpots(activity.getAvailableSpots())
                .catalogId(activity.getCatalog() != null ? activity.getCatalog().getId() : null)
                .catalogName(activity.getCatalog() != null ? activity.getCatalog().getName() : null)
                .catalogEmoji(activity.getCatalog() != null ? activity.getCatalog().getEmoji() : null)
                .registrationDeadline(activity.getRegistrationDeadline())
                .bookingOpen(isBookingOpen(activity))
                .build();
    }

    public static boolean isBookingOpen(Activity activity) {
        if (!activity.isActive() || activity.getAvailableSpots() <= 0) {
            return false;
        }
        if (activity.getRegistrationDeadline() == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(activity.getRegistrationDeadline());
    }

    public static void assertBookingOpen(Activity activity) {
        if (!isBookingOpen(activity)) {
            if (activity.getRegistrationDeadline() != null
                    && !LocalDateTime.now().isBefore(activity.getRegistrationDeadline())) {
                throw new BadRequestException("Les inscriptions sont closes pour cette activité");
            }
            throw new BadRequestException("Cette activité n'est plus disponible à la réservation");
        }
    }

    private void validateActivityDates(ActivityRequest request) {
        if (!request.getRegistrationDeadline().isBefore(request.getStartDateTime())) {
            throw new BadRequestException("La date limite d'inscription doit être avant le début de l'activité");
        }
    }
}
