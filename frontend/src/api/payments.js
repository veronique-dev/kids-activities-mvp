import { api } from '../api/client';

/**
 * Finalise un checkout : redirection Stripe ou paiement simulé (MOCK).
 * @returns {'redirect' | 'done'}
 */
export async function processPaymentCheckout(checkout) {
  if (checkout.provider === 'STRIPE' && checkout.checkoutUrl) {
    window.location.href = checkout.checkoutUrl;
    return 'redirect';
  }

  await api.completeMockPayment(checkout.paymentId);
  return 'done';
}
