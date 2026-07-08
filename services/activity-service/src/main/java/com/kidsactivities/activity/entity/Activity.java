package com.kidsactivities.activity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 5000)
    private String details;

    @Column(nullable = false, length = 2000)
    private String prerequisites;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer availableSpots;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "catalog_id")
    private Catalog catalog;

    @Column
    private LocalDateTime registrationDeadline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (availableSpots == null && maxCapacity != null) {
            availableSpots = maxCapacity;
        }
    }
}
