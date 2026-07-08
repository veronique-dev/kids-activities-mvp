package com.kidsactivities.payment.service;

import com.kidsactivities.common.dto.InternalBookingSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.exception.ResourceNotFoundException;
import com.kidsactivities.common.model.BookingStatus;
import com.kidsactivities.common.model.PaymentProvider;
import com.kidsactivities.common.model.PaymentStatus;
import com.kidsactivities.payment.config.StripeProperties;
import com.kidsactivities.payment.dto.response.CheckoutResponse;
import com.kidsactivities.payment.dto.response.PaymentResponse;
import com.kidsactivities.payment.entity.Payment;
import com.kidsactivities.payment.repository.PaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final StripeProperties stripeProperties;
    private final StripeCheckoutService stripeCheckoutService;

    @Transactional
    public CheckoutResponse createCheckout(Long userId, Long bookingId) {
        InternalBookingSnapshot booking = bookingServiceClient.getBooking(bookingId);
        validateBookingForPayment(userId, booking);

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.PENDING)
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .bookingId(bookingId)
                        .userId(userId)
                        .amount(booking.getAmount())
                        .currency(booking.getCurrency())
                        .status(PaymentStatus.PENDING)
                        .provider(PaymentProvider.MOCK)
                        .build()));

        if (stripeProperties.isConfigured()) {
            return createStripeCheckout(payment, booking);
        }

        payment.setProvider(PaymentProvider.MOCK);
        paymentRepository.save(payment);

        return CheckoutResponse.builder()
                .paymentId(payment.getId())
                .bookingId(bookingId)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .provider(PaymentProvider.MOCK)
                .checkoutUrl(null)
                .message("Paiement simulé — cliquez sur « Payer » pour finaliser la réservation")
                .build();
    }

    @Transactional
    public PaymentResponse completeMockPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        if (payment.getProvider() != PaymentProvider.MOCK) {
            throw new BadRequestException("Utilisez Stripe pour finaliser ce paiement");
        }

        return finalizePayment(payment, userId, "MOCK-" + payment.getId());
    }

    @Transactional
    public PaymentResponse verifyStripeSession(Long userId, String sessionId) {
        Session session = stripeCheckoutService.retrieveSession(sessionId);

        if (!"paid".equals(session.getPaymentStatus())) {
            throw new BadRequestException("Le paiement Stripe n'est pas encore confirmé");
        }

        Payment payment = findPaymentFromSession(session);
        if (!payment.getUserId().equals(userId)) {
            throw new BadRequestException("Ce paiement ne vous appartient pas");
        }

        return finalizePayment(payment, userId, session.getId());
    }

    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        Event event = stripeCheckoutService.constructWebhookEvent(payload, signatureHeader);

        if (!"checkout.session.completed".equals(event.getType())) {
            return;
        }

        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new BadRequestException("Événement Stripe invalide"));

        if (!"paid".equals(session.getPaymentStatus())) {
            return;
        }

        Payment payment = findPaymentFromSession(session);
        Long userId = Long.valueOf(session.getMetadata().get("userId"));
        finalizePayment(payment, userId, session.getId());
    }

    public PaymentResponse getPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
        return PaymentResponse.from(payment);
    }

    private CheckoutResponse createStripeCheckout(Payment payment, InternalBookingSnapshot booking) {
        Session session = stripeCheckoutService.createCheckoutSession(payment, booking);
        payment.setProvider(PaymentProvider.STRIPE);
        payment.setExternalReference(session.getId());
        paymentRepository.save(payment);

        return CheckoutResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .provider(PaymentProvider.STRIPE)
                .checkoutUrl(session.getUrl())
                .message("Vous allez être redirigé vers Stripe pour finaliser le paiement")
                .build();
    }

    private Payment findPaymentFromSession(Session session) {
        String sessionId = session.getId();
        Payment payment = paymentRepository.findByExternalReference(sessionId)
                .orElseGet(() -> {
                    String paymentId = session.getMetadata().get("paymentId");
                    if (paymentId == null) {
                        throw new ResourceNotFoundException("Paiement non trouvé pour cette session Stripe");
                    }
                    return paymentRepository.findById(Long.valueOf(paymentId))
                            .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
                });
        return payment;
    }

    private PaymentResponse finalizePayment(Payment payment, Long userId, String externalReference) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return PaymentResponse.from(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Ce paiement ne peut plus être finalisé");
        }

        if (!payment.getUserId().equals(userId)) {
            throw new BadRequestException("Ce paiement ne vous appartient pas");
        }

        InternalBookingSnapshot booking = bookingServiceClient.getBooking(payment.getBookingId());
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Cette réservation n'attend plus de paiement");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setExternalReference(externalReference);
        Payment saved = paymentRepository.save(payment);

        bookingServiceClient.confirmBooking(saved.getBookingId());
        log.info("Paiement {} finalisé pour réservation {}", saved.getId(), saved.getBookingId());

        return PaymentResponse.from(saved);
    }

    private void validateBookingForPayment(Long userId, InternalBookingSnapshot booking) {
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("Cette réservation ne vous appartient pas");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Cette réservation n'attend pas de paiement");
        }
        if (booking.getAmount() == null || booking.getAmount().signum() <= 0) {
            throw new BadRequestException("Aucun paiement requis pour cette réservation");
        }
    }
}
