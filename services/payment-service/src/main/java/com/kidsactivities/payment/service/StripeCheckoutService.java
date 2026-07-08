package com.kidsactivities.payment.service;

import com.kidsactivities.common.dto.InternalBookingSnapshot;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.payment.config.StripeProperties;
import com.kidsactivities.payment.entity.Payment;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutService {

    private final StripeProperties stripeProperties;

    public Session createCheckoutSession(Payment payment, InternalBookingSnapshot booking) {
        if (!stripeProperties.isConfigured()) {
            throw new BadRequestException("Stripe n'est pas configuré");
        }

        Stripe.apiKey = stripeProperties.getSecretKey();

        long amountCents = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String currency = payment.getCurrency() != null
                ? payment.getCurrency().toLowerCase()
                : "eur";

        String successUrl = stripeProperties.getFrontendUrl()
                + "/bookings?payment=success&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = stripeProperties.getFrontendUrl()
                + "/bookings?payment=cancelled";

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("bookingId", payment.getBookingId().toString())
                    .putMetadata("userId", payment.getUserId().toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(amountCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Réservation — " + booking.getChildName())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session;
        } catch (StripeException e) {
            log.error("Erreur Stripe Checkout : {}", e.getMessage());
            throw new BadRequestException("Impossible de créer la session de paiement Stripe");
        }
    }

    public Session retrieveSession(String sessionId) {
        if (!stripeProperties.isConfigured()) {
            throw new BadRequestException("Stripe n'est pas configuré");
        }
        Stripe.apiKey = stripeProperties.getSecretKey();
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            log.error("Session Stripe introuvable : {}", sessionId);
            throw new BadRequestException("Session de paiement invalide");
        }
    }

    public Event constructWebhookEvent(String payload, String signatureHeader) {
        String webhookSecret = stripeProperties.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new BadRequestException("Webhook Stripe non configuré");
        }
        try {
            return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Signature webhook Stripe invalide");
        }
    }
}
