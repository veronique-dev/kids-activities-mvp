package com.kidsactivities.payment.service;

import com.kidsactivities.common.dto.InternalBookingSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.model.BookingStatus;
import com.kidsactivities.common.model.PaymentProvider;
import com.kidsactivities.common.model.PaymentStatus;
import com.kidsactivities.payment.config.StripeProperties;
import com.kidsactivities.payment.dto.response.CheckoutResponse;
import com.kidsactivities.payment.dto.response.PaymentResponse;
import com.kidsactivities.payment.entity.Payment;
import com.kidsactivities.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingServiceClient bookingServiceClient;

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createCheckout_shouldCreatePendingPayment() {
        when(stripeProperties.isConfigured()).thenReturn(false);
        InternalBookingSnapshot booking = InternalBookingSnapshot.builder()
                .id(10L)
                .userId(1L)
                .activityId(2L)
                .status(BookingStatus.PENDING_PAYMENT)
                .amount(new BigDecimal("25.00"))
                .currency("EUR")
                .childName("Lucas")
                .build();

        when(bookingServiceClient.getBooking(10L)).thenReturn(booking);
        when(paymentRepository.findByBookingIdAndStatus(10L, PaymentStatus.PENDING)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            return payment;
        });

        CheckoutResponse response = paymentService.createCheckout(1L, 10L);

        assertThat(response.getPaymentId()).isEqualTo(99L);
        assertThat(response.getProvider()).isEqualTo(PaymentProvider.MOCK);
        assertThat(response.getAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void completeMockPayment_shouldConfirmBooking() {
        Payment payment = Payment.builder()
                .id(99L)
                .bookingId(10L)
                .userId(1L)
                .amount(new BigDecimal("25.00"))
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .provider(PaymentProvider.MOCK)
                .build();

        InternalBookingSnapshot booking = InternalBookingSnapshot.builder()
                .id(10L)
                .userId(1L)
                .status(BookingStatus.PENDING_PAYMENT)
                .amount(new BigDecimal("25.00"))
                .currency("EUR")
                .build();

        when(paymentRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.of(payment));
        when(bookingServiceClient.getBooking(10L)).thenReturn(booking);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.completeMockPayment(1L, 99L);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(bookingServiceClient).confirmBooking(10L);
    }

    @Test
    void createCheckout_shouldRejectWhenNotOwner() {
        InternalBookingSnapshot booking = InternalBookingSnapshot.builder()
                .id(10L)
                .userId(2L)
                .status(BookingStatus.PENDING_PAYMENT)
                .amount(new BigDecimal("25.00"))
                .currency("EUR")
                .build();

        when(bookingServiceClient.getBooking(10L)).thenReturn(booking);

        assertThatThrownBy(() -> paymentService.createCheckout(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cette réservation ne vous appartient pas");
    }
}
