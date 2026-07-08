package com.kidsactivities.activity.controller;

import com.kidsactivities.activity.dto.CatalogResponse;
import com.kidsactivities.activity.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
@Tag(name = "Catalogues", description = "Catalogues d'activités")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    @Operation(summary = "Liste des catalogues actifs")
    public List<CatalogResponse> getActiveCatalogs() {
        return catalogService.getActiveCatalogs();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Liste de tous les catalogues (admin)")
    public List<CatalogResponse> getAllCatalogs() {
        return catalogService.getAllCatalogs();
    }
}
