package com.kidsactivities.activity.repository;

import com.kidsactivities.activity.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    List<Catalog> findByActiveTrueOrderBySortOrderAscNameAsc();
}
