package com.kidsactivities.payment.dto.response;

import com.kidsactivities.common.model.PaymentProvider;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CheckoutResponse {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private PaymentProvider provider;
    private String checkoutUrl;
    private String message;
}
