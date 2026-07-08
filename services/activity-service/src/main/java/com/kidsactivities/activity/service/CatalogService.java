package com.kidsactivities.activity.service;

import com.kidsactivities.activity.dto.CatalogResponse;
import com.kidsactivities.activity.entity.Catalog;
import com.kidsactivities.activity.repository.ActivityRepository;
import com.kidsactivities.activity.repository.CatalogRepository;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final ActivityRepository activityRepository;

    public List<CatalogResponse> getActiveCatalogs() {
        return catalogRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    public List<CatalogResponse> getAllCatalogs() {
        return catalogRepository.findAll().stream()
                .sorted((a, b) -> {
                    int order = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return order != 0 ? order : a.getName().compareToIgnoreCase(b.getName());
                })
                .map(this::toResponseWithCount)
                .toList();
    }

    public Catalog findCatalog(Long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalogue non trouvé"));
    }

    private CatalogResponse toResponseWithCount(Catalog catalog) {
        long count = activityRepository.countByCatalogIdAndActiveTrue(catalog.getId());
        return CatalogResponse.from(catalog, count);
    }
}
