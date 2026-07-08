package com.kidsactivities.activity.service;

import com.kidsactivities.activity.dto.ActivityRequest;
import com.kidsactivities.activity.dto.ActivityResponse;
import com.kidsactivities.activity.entity.Activity;
import com.kidsactivities.activity.repository.ActivityRepository;
import com.kidsactivities.common.dto.ActivitySnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public List<ActivityResponse> getActiveActivities() {
        return activityRepository.findByActiveTrueOrderByStartDateTimeAsc().stream()
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
        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDateTime(request.getStartDateTime())
                .location(request.getLocation())
                .maxCapacity(request.getMaxCapacity())
                .availableSpots(request.getMaxCapacity())
                .price(request.getPrice())
                .active(request.isActive())
                .build();

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        Activity activity = findActivity(id);

        int bookedSpots = activity.getMaxCapacity() - activity.getAvailableSpots();
        int newMaxCapacity = request.getMaxCapacity();
        if (newMaxCapacity < bookedSpots) {
            throw new BadRequestException(
                    "La capacité ne peut pas être inférieure au nombre de places déjà réservées"
            );
        }

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setStartDateTime(request.getStartDateTime());
        activity.setLocation(request.getLocation());
        activity.setMaxCapacity(newMaxCapacity);
        activity.setAvailableSpots(newMaxCapacity - bookedSpots);
        activity.setPrice(request.getPrice());
        activity.setActive(request.isActive());

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
                .build();
    }
}
