package com.kidsactivities.payment.controller;

import com.kidsactivities.payment.dto.request.CheckoutRequest;
import com.kidsactivities.payment.dto.response.CheckoutResponse;
import com.kidsactivities.payment.dto.response.PaymentResponse;
import com.kidsactivities.payment.security.AuthenticatedUser;
import com.kidsactivities.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Gestion des paiements de réservation")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Initier le paiement d'une réservation")
    public CheckoutResponse checkout(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return paymentService.createCheckout(user.getId(), request.getBookingId());
    }

    @PostMapping("/{id}/complete-mock")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Finaliser un paiement simulé (développement / démo)")
    public PaymentResponse completeMockPayment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return paymentService.completeMockPayment(user.getId(), id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Consulter un paiement")
    public PaymentResponse getPayment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return paymentService.getPayment(user.getId(), id);
    }

    @PostMapping("/verify-session")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Confirmer un paiement Stripe après redirection")
    public PaymentResponse verifyStripeSession(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam String sessionId
    ) {
        return paymentService.verifyStripeSession(user.getId(), sessionId);
    }
}
