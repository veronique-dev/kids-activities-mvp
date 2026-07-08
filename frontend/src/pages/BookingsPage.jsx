import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { processPaymentCheckout } from '../api/payments';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function statusLabel(status) {
  switch (status) {
    case 'CONFIRMED':
      return 'Confirmée';
    case 'PENDING_PAYMENT':
      return 'En attente de paiement';
    case 'CANCELLED':
      return 'Annulée';
    default:
      return status;
  }
}

function statusBadgeClass(status) {
  switch (status) {
    case 'CONFIRMED':
      return 'badge-success';
    case 'PENDING_PAYMENT':
      return 'badge-warning';
    case 'CANCELLED':
      return 'badge-danger';
    default:
      return '';
  }
}

export default function BookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [payingId, setPayingId] = useState(null);

  async function loadBookings() {
    setLoading(true);
    try {
      const data = await api.getBookings();
      setBookings(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const sessionId = params.get('session_id');
    const paymentStatus = params.get('payment');

    if (paymentStatus === 'cancelled') {
      setError('Paiement annulé. Vous pouvez réessayer depuis vos réservations.');
      window.history.replaceState({}, '', '/bookings');
      loadBookings();
      return;
    }

    if (sessionId) {
      setLoading(true);
      api.verifyStripeSession(sessionId)
        .then(() => {
          setSuccess('Paiement Stripe confirmé — votre réservation est validée.');
          return loadBookings();
        })
        .catch((err) => setError(err.message))
        .finally(() => {
          window.history.replaceState({}, '', '/bookings');
        });
      return;
    }

    loadBookings();
  }, []);

  async function handleCancel(id) {
    if (!window.confirm('Annuler cette réservation ?')) return;
    try {
      await api.cancelBooking(id);
      await loadBookings();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handlePay(booking) {
    setPayingId(booking.id);
    setError('');
    try {
      const checkout = await api.createPaymentCheckout(booking.id);
      await processPaymentCheckout(checkout);
      if (checkout.provider !== 'STRIPE') {
        await loadBookings();
        setPayingId(null);
      }
    } catch (err) {
      setError(err.message);
      setPayingId(null);
    }
  }

  return (
    <>
      <section className="hero">
        <h1>Mes réservations</h1>
        <p>Suivez et gérez les inscriptions de vos enfants.</p>
      </section>

      {success && <div className="success-banner">{success}</div>}
      {error && <div className="error-banner">{error}</div>}
      {loading && <div className="empty-state">Chargement...</div>}

      {!loading && bookings.length === 0 && (
        <div className="empty-state">Aucune réservation pour le moment.</div>
      )}

      {!loading && bookings.length > 0 && (
        <div className="grid">
          {bookings.map((booking) => (
            <article key={booking.id} className="card">
              <h3>{booking.activityTitle}</h3>
              <div className="card-meta">
                <span>{formatDate(booking.activityStartDateTime)}</span>
                <span>Enfant : {booking.childName} ({booking.childAge} ans)</span>
                {booking.amount != null && booking.amount > 0 && (
                  <span>Montant : {booking.amount} {booking.currency || '€'}</span>
                )}
                <span>
                  Statut :{' '}
                  <span className={`badge ${statusBadgeClass(booking.status)}`}>
                    {statusLabel(booking.status)}
                  </span>
                </span>
              </div>
              {booking.status === 'PENDING_PAYMENT' && (
                <button
                  className="btn btn-primary"
                  onClick={() => handlePay(booking)}
                  disabled={payingId === booking.id}
                >
                  {payingId === booking.id ? 'Redirection...' : `Payer ${booking.amount} €`}
                </button>
              )}
              {booking.status === 'CONFIRMED' && (
                <button className="btn btn-danger" onClick={() => handleCancel(booking.id)}>
                  Annuler la réservation
                </button>
              )}
              {booking.status === 'PENDING_PAYMENT' && (
                <button className="btn btn-danger btn-outline" onClick={() => handleCancel(booking.id)}>
                  Annuler
                </button>
              )}
            </article>
          ))}
        </div>
      )}
    </>
  );
}
