package com.kidsactivities.booking.repository;

import com.kidsactivities.booking.entity.Booking;
import com.kidsactivities.common.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByStatus(BookingStatus status);
    boolean existsByUserIdAndActivityIdAndStatus(Long userId, Long activityId, BookingStatus status);
    boolean existsByUserIdAndActivityIdAndStatusIn(Long userId, Long activityId, Collection<BookingStatus> statuses);
}
