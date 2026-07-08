package com.kidsactivities.service;

import com.kidsactivities.dto.request.ActivityRequest;
import com.kidsactivities.dto.response.ActivityResponse;
import com.kidsactivities.entity.Activity;
import com.kidsactivities.exception.ResourceNotFoundException;
import com.kidsactivities.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new com.kidsactivities.exception.BadRequestException(
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

    Activity findActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activité non trouvée"));
    }
}
