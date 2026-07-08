package com.kidsactivities.activity.repository;

import com.kidsactivities.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByActiveTrueOrderByStartDateTimeAsc();

    List<Activity> findByActiveTrueAndCatalogIdOrderByStartDateTimeAsc(Long catalogId);

    long countByCatalogIdAndActiveTrue(Long catalogId);
}
