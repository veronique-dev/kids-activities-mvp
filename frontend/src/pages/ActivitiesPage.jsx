import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import ActivityDetailModal from '../components/ActivityDetailModal';
import BookingModal from '../components/BookingModal';
import { activityEmoji, cardThemeClass } from '../utils/activityEmoji';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function truncate(text, max = 110) {
  if (!text || text.length <= max) return text;
  return `${text.slice(0, max).trim()}…`;
}

export default function ActivitiesPage() {
  const { isAuthenticated } = useAuth();
  const [catalogs, setCatalogs] = useState([]);
  const [selectedCatalogId, setSelectedCatalogId] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [detailActivity, setDetailActivity] = useState(null);
  const [bookingActivity, setBookingActivity] = useState(null);

  useEffect(() => {
    api.getCatalogs()
      .then(setCatalogs)
      .catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    setLoading(true);
    api.getActivities(selectedCatalogId)
      .then(setActivities)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [selectedCatalogId]);

  const totalSpots = activities.reduce((sum, a) => sum + a.availableSpots, 0);

  function getBookingLabel(activity) {
    if (!activity.bookingOpen) {
      return activity.availableSpots === 0 ? 'Complet' : 'Inscriptions closes';
    }
    return 'Réserver 🎈';
  }

  function refreshActivities() {
    api.getActivities(selectedCatalogId).then(setActivities);
    api.getCatalogs().then(setCatalogs);
  }

  function openBooking(activity) {
    setDetailActivity(null);
    setBookingActivity(activity);
  }

  return (
    <>
      <section className="hero hero-fun">
        <div className="hero-content">
          <span className="hero-badge">✨ Nouvelles aventures chaque semaine</span>
          <h1>Des activités <span className="text-gradient">fun</span> pour vos enfants</h1>
          <p>Parcourez nos catalogues, consultez les détails et réservez avant la date limite !</p>
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
            <strong>{catalogs.length}</strong>
            <span>catalogues</span>
          </div>
        </div>
      </section>

      {catalogs.length > 0 && (
        <section className="catalog-nav" aria-label="Filtrer par catalogue">
          <button
            type="button"
            className={`catalog-chip ${selectedCatalogId === null ? 'catalog-chip-active' : ''}`}
            onClick={() => setSelectedCatalogId(null)}
          >
            🌈 Tous
          </button>
          {catalogs.map((catalog) => (
            <button
              key={catalog.id}
              type="button"
              className={`catalog-chip ${selectedCatalogId === catalog.id ? 'catalog-chip-active' : ''}`}
              onClick={() => setSelectedCatalogId(catalog.id)}
            >
              {catalog.emoji} {catalog.name}
              <span className="catalog-count">{catalog.activityCount}</span>
            </button>
          ))}
        </section>
      )}

      {error && <div className="error-banner">{error}</div>}
      {loading && <div className="empty-state">🔍 Chargement des activités...</div>}

      {!loading && activities.length === 0 && (
        <div className="empty-state empty-fun">
          <span className="empty-icon">🎪</span>
          <p>Aucune activité dans ce catalogue pour le moment.</p>
        </div>
      )}

      <div className="grid">
        {activities.map((activity, index) => (
          <article key={activity.id} className={`card activity-card ${cardThemeClass(index)}`}>
            <div className="card-header">
              <span className="activity-emoji" aria-hidden="true">
                {activity.catalogEmoji || activityEmoji(activity.title)}
              </span>
              <span className="price-tag">
                {activity.price > 0 ? `${activity.price} €` : 'Gratuit 🎁'}
              </span>
            </div>
            {activity.catalogName && (
              <span className="catalog-badge">{activity.catalogEmoji} {activity.catalogName}</span>
            )}
            <h3>{activity.title}</h3>
            <p className="card-desc">{truncate(activity.description)}</p>
            <div className="card-meta">
              <span>📅 {formatDate(activity.startDateTime)}</span>
              <span>📍 {activity.location}</span>
              <span className={activity.registrationDeadline && !activity.bookingOpen ? 'deadline-closed' : 'deadline-open'}>
                ⏰ Inscriptions jusqu'au {formatDate(activity.registrationDeadline)}
              </span>
              <span className={activity.availableSpots === 0 ? 'spots-full' : 'spots-ok'}>
                {activity.availableSpots === 0
                  ? '😢 Complet'
                  : `🎟️ ${activity.availableSpots} place${activity.availableSpots > 1 ? 's' : ''}`}
              </span>
            </div>
            <div className="card-actions">
              <button
                type="button"
                className="btn btn-secondary btn-block"
                onClick={() => setDetailActivity(activity)}
              >
                Voir les détails 📖
              </button>
              {isAuthenticated ? (
                <button
                  className="btn btn-primary btn-block"
                  disabled={!activity.bookingOpen}
                  onClick={() => setBookingActivity(activity)}
                >
                  {getBookingLabel(activity)}
                </button>
              ) : (
                <Link to="/login" className="btn btn-primary btn-block">
                  Se connecter pour réserver
                </Link>
              )}
            </div>
          </article>
        ))}
      </div>

      {detailActivity && (
        <ActivityDetailModal
          activity={detailActivity}
          isAuthenticated={isAuthenticated}
          onClose={() => setDetailActivity(null)}
          onBook={openBooking}
        />
      )}

      {bookingActivity && (
        <BookingModal
          activity={bookingActivity}
          onClose={() => setBookingActivity(null)}
          onSuccess={() => {
            setBookingActivity(null);
            refreshActivities();
          }}
        />
      )}
    </>
  );
}
