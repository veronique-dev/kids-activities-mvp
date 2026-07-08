import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import BookingModal from '../components/BookingModal';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

export default function ActivitiesPage() {
  const { isAuthenticated } = useAuth();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedActivity, setSelectedActivity] = useState(null);

  useEffect(() => {
    api.getActivities()
      .then(setActivities)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <section className="hero">
        <h1>Activités pour enfants</h1>
        <p>Découvrez et réservez des activités près de chez vous.</p>
      </section>

      {error && <div className="error-banner">{error}</div>}
      {loading && <div className="empty-state">Chargement des activités...</div>}

      {!loading && activities.length === 0 && (
        <div className="empty-state">Aucune activité disponible pour le moment.</div>
      )}

      <div className="grid">
        {activities.map((activity) => (
          <article key={activity.id} className="card">
            <h3>{activity.title}</h3>
            <p>{activity.description}</p>
            <div className="card-meta">
              <span>{formatDate(activity.startDateTime)}</span>
              <span>{activity.location}</span>
              <span>{activity.price} €</span>
              <span>{activity.availableSpots} place(s) restante(s)</span>
            </div>
            {isAuthenticated ? (
              <button
                className="btn btn-primary"
                disabled={activity.availableSpots === 0}
                onClick={() => setSelectedActivity(activity)}
              >
                Réserver
              </button>
            ) : (
              <span className="badge badge-muted">Connectez-vous pour réserver</span>
            )}
          </article>
        ))}
      </div>

      {selectedActivity && (
        <BookingModal
          activity={selectedActivity}
          onClose={() => setSelectedActivity(null)}
          onSuccess={() => {
            setSelectedActivity(null);
            api.getActivities().then(setActivities);
          }}
        />
      )}
    </>
  );
}
