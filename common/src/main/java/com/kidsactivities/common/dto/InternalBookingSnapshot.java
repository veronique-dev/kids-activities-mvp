package com.kidsactivities.common.dto;

import com.kidsactivities.common.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalBookingSnapshot {
    private Long id;
    private Long userId;
    private Long activityId;
    private BookingStatus status;
    private BigDecimal amount;
    private String currency;
    private String childName;
}
