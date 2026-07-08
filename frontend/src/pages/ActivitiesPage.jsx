import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import BookingModal from '../components/BookingModal';
import { activityEmoji, cardThemeClass } from '../utils/activityEmoji';

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

  const totalSpots = activities.reduce((sum, a) => sum + a.availableSpots, 0);

  return (
    <>
      <section className="hero hero-fun">
        <div className="hero-content">
          <span className="hero-badge">✨ Nouvelles aventures chaque semaine</span>
          <h1>Des activités <span className="text-gradient">fun</span> pour vos enfants</h1>
          <p>Théâtre, peinture, sport, natation… Réservez en quelques clics et partez à l'aventure !</p>
          {!isAuthenticated && (
            <Link to="/register" className="btn btn-accent btn-lg">
              Créer un compte parent 🚀
            </Link>
          )}
        </div>
        <div className="hero-stats">
          <div className="hero-stat">
            <strong>{activities.length}</strong>
            <span>activités</span>
          </div>
          <div className="hero-stat">
            <strong>{totalSpots}</strong>
            <span>places dispo</span>
          </div>
          <div className="hero-stat">
            <strong>🎉</strong>
            <span>100% fun</span>
          </div>
        </div>
      </section>

      {error && <div className="error-banner">{error}</div>}
      {loading && <div className="empty-state">🔍 Chargement des activités...</div>}

      {!loading && activities.length === 0 && (
        <div className="empty-state empty-fun">
          <span className="empty-icon">🎪</span>
          <p>Aucune activité pour le moment — revenez bientôt !</p>
        </div>
      )}

      <div className="grid">
        {activities.map((activity, index) => (
          <article key={activity.id} className={`card activity-card ${cardThemeClass(index)}`}>
            <div className="card-header">
              <span className="activity-emoji" aria-hidden="true">{activityEmoji(activity.title)}</span>
              <span className="price-tag">
                {activity.price > 0 ? `${activity.price} €` : 'Gratuit 🎁'}
              </span>
            </div>
            <h3>{activity.title}</h3>
            <p className="card-desc">{activity.description}</p>
            <div className="card-meta">
              <span>📅 {formatDate(activity.startDateTime)}</span>
              <span>📍 {activity.location}</span>
              <span className={activity.availableSpots === 0 ? 'spots-full' : 'spots-ok'}>
                {activity.availableSpots === 0
                  ? '😢 Complet'
                  : `🎟️ ${activity.availableSpots} place${activity.availableSpots > 1 ? 's' : ''}`}
              </span>
            </div>
            {isAuthenticated ? (
              <button
                className="btn btn-primary btn-block"
                disabled={activity.availableSpots === 0}
                onClick={() => setSelectedActivity(activity)}
              >
                {activity.availableSpots === 0 ? 'Plus de places' : 'Réserver 🎈'}
              </button>
            ) : (
              <Link to="/login" className="btn btn-secondary btn-block">
                Se connecter pour réserver
              </Link>
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
