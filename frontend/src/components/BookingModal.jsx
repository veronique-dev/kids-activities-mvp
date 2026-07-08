import { useState } from 'react';
import { api } from '../api/client';

export default function BookingModal({ activity, onClose, onSuccess }) {
  const [form, setForm] = useState({ childName: '', childAge: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await api.createBooking({
        activityId: activity.id,
        childName: form.childName,
        childAge: Number(form.childAge),
      });
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>Réserver : {activity.title}</h2>
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
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
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Annuler
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Réservation...' : 'Confirmer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
