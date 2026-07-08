package com.kidsactivities.activity.dto;

import com.kidsactivities.activity.entity.Catalog;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatalogResponse {
    private Long id;
    private String name;
    private String description;
    private String emoji;
    private boolean active;
    private int sortOrder;
    private long activityCount;

    public static CatalogResponse from(Catalog catalog, long activityCount) {
        return CatalogResponse.builder()
                .id(catalog.getId())
                .name(catalog.getName())
                .description(catalog.getDescription())
                .emoji(catalog.getEmoji())
                .active(catalog.isActive())
                .sortOrder(catalog.getSortOrder())
                .activityCount(activityCount)
                .build();
    }
}
