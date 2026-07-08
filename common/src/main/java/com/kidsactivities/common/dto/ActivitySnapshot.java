package com.kidsactivities.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySnapshot {
    private Long id;
    private String title;
    private LocalDateTime startDateTime;
    private String location;
    private BigDecimal price;
    private boolean active;
    private int availableSpots;
    private Long catalogId;
    private String catalogName;
    private String catalogEmoji;
    private LocalDateTime registrationDeadline;
    private boolean bookingOpen;
}
