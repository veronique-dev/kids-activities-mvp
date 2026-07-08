import { useState } from 'react';
import { activityEmoji } from '../utils/activityEmoji';
import { api } from '../api/client';
import { processPaymentCheckout } from '../api/payments';

export default function BookingModal({ activity, onClose, onSuccess }) {
  const [form, setForm] = useState({ childName: '', childAge: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('form');
  const [booking, setBooking] = useState(null);
  const [checkout, setCheckout] = useState(null);

  const isPaid = activity.price > 0;
  const isStripe = checkout?.provider === 'STRIPE';

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const created = await api.createBooking({
        activityId: activity.id,
        childName: form.childName,
        childAge: Number(form.childAge),
      });

      if (created.paymentRequired) {
        setBooking(created);
        const paymentCheckout = await api.createPaymentCheckout(created.id);
        setCheckout(paymentCheckout);
        setStep('payment');
      } else {
        onSuccess();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handlePay() {
    setError('');
    setLoading(true);
    try {
      const result = await processPaymentCheckout(checkout);
      if (result === 'done') {
        onSuccess();
      }
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal modal-fun" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <span className="activity-emoji" aria-hidden="true">{activityEmoji(activity.title)}</span>
          <h2>Réserver : {activity.title}</h2>
        </div>
        {error && <div className="error-banner">{error}</div>}

        {step === 'form' && (
          <form onSubmit={handleSubmit}>
            <p className="modal-summary">{activity.description}</p>
            {activity.prerequisites && (
              <div className="prerequisites-box">
                <strong>Prérequis à vérifier</strong>
                <p>{activity.prerequisites}</p>
              </div>
            )}
            <div className="form-group">
              <label htmlFor="childName">Prénom de l'enfant</label>
              <input
                id="childName"
                value={form.childName}
                onChange={(e) => setForm({ ...form, childName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="childAge">Âge de l'enfant</label>
              <input
                id="childAge"
                type="number"
                min="1"
                max="18"
                value={form.childAge}
                onChange={(e) => setForm({ ...form, childAge: e.target.value })}
                required
              />
            </div>
            {isPaid && (
              <div className="payment-summary payment-summary-prominent">
                <strong>Paiement requis</strong>
                <div>Montant : {activity.price} €</div>
              </div>
            )}
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Annuler
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Traitement...' : isPaid ? 'Continuer vers le paiement' : 'Confirmer'}
              </button>
            </div>
          </form>
        )}

        {step === 'payment' && booking && checkout && (
          <div className="payment-step">
            <p>
              Réservation pour <strong>{booking.childName}</strong> ({booking.childAge} ans)
            </p>
            <p className="payment-summary">
              Total : <strong>{checkout.amount} {checkout.currency}</strong>
            </p>
            <p className="payment-hint">{checkout.message}</p>
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Annuler
              </button>
              <button type="button" className="btn btn-primary" onClick={handlePay} disabled={loading}>
                {loading
                  ? 'Redirection...'
                  : isStripe
                    ? `Payer ${checkout.amount} € avec Stripe`
                    : `Payer ${checkout.amount} €`}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
