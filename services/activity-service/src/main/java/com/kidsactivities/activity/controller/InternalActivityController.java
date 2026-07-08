package com.kidsactivities.activity.controller;

import com.kidsactivities.activity.service.ActivityService;
import com.kidsactivities.common.dto.ActivitySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalActivityController {

    private final ActivityService activityService;

    @GetMapping("/activities/{id}")
    public ActivitySnapshot getActivity(@PathVariable Long id) {
        return activityService.toSnapshot(activityService.findActivity(id));
    }

    @PostMapping("/activities/{id}/reserve")
    public ActivitySnapshot reserveSpot(@PathVariable Long id) {
        return activityService.toSnapshot(activityService.reserveSpot(id));
    }

    @PostMapping("/activities/{id}/release")
    public ActivitySnapshot releaseSpot(@PathVariable Long id) {
        return activityService.toSnapshot(activityService.releaseSpot(id));
    }

    @GetMapping("/stats/count")
    public long countActivities() {
        return activityService.countActivities();
    }
}
