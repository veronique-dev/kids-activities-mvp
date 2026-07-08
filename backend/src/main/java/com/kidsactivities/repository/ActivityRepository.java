package com.kidsactivities.repository;

import com.kidsactivities.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByActiveTrueOrderByStartDateTimeAsc();
}
