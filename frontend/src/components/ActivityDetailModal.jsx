import { Link } from 'react-router-dom';
import { activityEmoji } from '../utils/activityEmoji';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function formatLines(text) {
  if (!text) return null;
  return text.trim().split('\n').filter(Boolean);
}

export default function ActivityDetailModal({ activity, isAuthenticated, onClose, onBook }) {
  const detailLines = formatLines(activity.details);
  const prerequisiteLines = formatLines(activity.prerequisites);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal modal-fun modal-detail" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header modal-detail-header">
          <span className="activity-emoji activity-emoji-lg" aria-hidden="true">
            {activity.catalogEmoji || activityEmoji(activity.title)}
          </span>
          <div>
            {activity.catalogName && (
              <span className="catalog-badge">{activity.catalogEmoji} {activity.catalogName}</span>
            )}
            <h2>{activity.title}</h2>
          </div>
        </div>

        <div className="activity-detail-body">
          <section className="detail-section">
            <h3>En bref</h3>
            <p>{activity.description}</p>
          </section>

          <section className="detail-section detail-info-grid">
            <div><strong>📅 Début</strong><span>{formatDate(activity.startDateTime)}</span></div>
            <div><strong>📍 Lieu</strong><span>{activity.location}</span></div>
            <div><strong>💶 Tarif</strong><span>{activity.price > 0 ? `${activity.price} €` : 'Gratuit'}</span></div>
            <div><strong>🎟️ Places</strong><span>{activity.availableSpots} / {activity.maxCapacity}</span></div>
            <div>
              <strong>⏰ Inscriptions</strong>
              <span className={activity.bookingOpen ? 'deadline-open' : 'deadline-closed'}>
                jusqu'au {formatDate(activity.registrationDeadline)}
              </span>
            </div>
          </section>

          {detailLines && (
            <section className="detail-section">
              <h3>Programme & détails</h3>
              <ul className="detail-list">
                {detailLines.map((line) => (
                  <li key={line}>{line}</li>
                ))}
              </ul>
            </section>
          )}

          {prerequisiteLines && (
            <section className="detail-section detail-prerequisites">
              <h3>Prérequis</h3>
              <ul className="detail-list detail-list-check">
                {prerequisiteLines.map((line) => (
                  <li key={line}>{line.replace(/^•\s*/, '')}</li>
                ))}
              </ul>
            </section>
          )}
        </div>

        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Fermer
          </button>
          {isAuthenticated ? (
            <button
              type="button"
              className="btn btn-primary"
              disabled={!activity.bookingOpen}
              onClick={() => onBook(activity)}
            >
              {activity.bookingOpen ? 'Réserver cette activité 🎈' : 'Inscriptions closes'}
            </button>
          ) : (
            <Link to="/login" className="btn btn-primary">
              Se connecter pour réserver
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
