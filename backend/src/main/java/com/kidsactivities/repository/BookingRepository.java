package com.kidsactivities.repository;

import com.kidsactivities.entity.Booking;
import com.kidsactivities.entity.BookingStatus;
import com.kidsactivities.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByStatus(BookingStatus status);
    long countByActivityIdAndStatus(Long activityId, BookingStatus status);
    boolean existsByUserIdAndActivityIdAndStatus(Long userId, Long activityId, BookingStatus status);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
