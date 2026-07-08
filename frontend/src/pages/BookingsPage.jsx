import { useEffect, useState } from 'react';
import { api } from '../api/client';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

export default function BookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  return (
    <>
      <section className="hero">
        <h1>Mes réservations</h1>
        <p>Suivez et gérez les inscriptions de vos enfants.</p>
      </section>

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
                <span>
                  Statut :{' '}
                  <span className={`badge ${booking.status === 'CONFIRMED' ? 'badge-success' : 'badge-danger'}`}>
                    {booking.status === 'CONFIRMED' ? 'Confirmée' : 'Annulée'}
                  </span>
                </span>
              </div>
              {booking.status === 'CONFIRMED' && (
                <button className="btn btn-danger" onClick={() => handleCancel(booking.id)}>
                  Annuler la réservation
                </button>
              )}
            </article>
          ))}
        </div>
      )}
    </>
  );
}
