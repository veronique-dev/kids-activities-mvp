package com.kidsactivities.controller;

import com.kidsactivities.dto.request.ActivityRequest;
import com.kidsactivities.dto.response.ActivityResponse;
import com.kidsactivities.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activités", description = "Gestion des activités")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @Operation(summary = "Liste des activités actives")
    public List<ActivityResponse> getActiveActivities() {
        return activityService.getActiveActivities();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une activité")
    public ActivityResponse getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Liste de toutes les activités (admin)")
    public List<ActivityResponse> getAllActivities() {
        return activityService.getAllActivities();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Créer une activité (admin)")
    public ActivityResponse createActivity(@Valid @RequestBody ActivityRequest request) {
        return activityService.createActivity(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Modifier une activité (admin)")
    public ActivityResponse updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request
    ) {
        return activityService.updateActivity(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Supprimer une activité (admin)")
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }
}
