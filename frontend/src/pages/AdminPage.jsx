import { useEffect, useState } from 'react';
import { api } from '../api/client';

function formatDate(value) {
  return new Date(value).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

const emptyActivityForm = {
  title: '',
  description: '',
  details: '',
  prerequisites: '',
  startDateTime: '',
  registrationDeadline: '',
  location: '',
  maxCapacity: 10,
  price: 0,
  catalogId: '',
  active: true,
};

export default function AdminPage() {
  const [dashboard, setDashboard] = useState(null);
  const [activities, setActivities] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(emptyActivityForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function loadData() {
    setLoading(true);
    try {
      const [dashboardData, activitiesData, catalogsData, usersData] = await Promise.all([
        api.getDashboard(),
        api.getAllActivities(),
        api.getAllCatalogs(),
        api.getUsers(),
      ]);
      setDashboard(dashboardData);
      setActivities(activitiesData);
      setCatalogs(catalogsData);
      setUsers(usersData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function startEdit(activity) {
    setEditingId(activity.id);
    setForm({
      title: activity.title,
      description: activity.description,
      details: activity.details || '',
      prerequisites: activity.prerequisites || '',
      startDateTime: activity.startDateTime.slice(0, 16),
      registrationDeadline: activity.registrationDeadline.slice(0, 16),
      location: activity.location,
      maxCapacity: activity.maxCapacity,
      price: activity.price,
      catalogId: activity.catalogId,
      active: activity.active,
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    const payload = {
      ...form,
      maxCapacity: Number(form.maxCapacity),
      price: Number(form.price),
      catalogId: Number(form.catalogId),
      active: Boolean(form.active),
    };

    try {
      if (editingId) {
        await api.updateActivity(editingId, payload);
      } else {
        await api.createActivity(payload);
      }
      setForm(emptyActivityForm);
      setEditingId(null);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Supprimer cette activité ?')) return;
    try {
      await api.deleteActivity(id);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) {
    return <div className="empty-state">Chargement du tableau de bord...</div>;
  }

  return (
    <>
      <section className="hero hero-compact hero-admin">
        <h1>⚙️ Administration</h1>
        <p>Vue d'ensemble et gestion des activités.</p>
      </section>

      {error && <div className="error-banner">{error}</div>}

      {dashboard && (
        <section className="stats-grid">
          <div className="stat-card stat-purple"><strong>{dashboard.totalUsers}</strong>👥 Utilisateurs</div>
          <div className="stat-card stat-coral"><strong>{dashboard.totalActivities}</strong>🎨 Activités</div>
          <div className="stat-card stat-mint"><strong>{dashboard.totalBookings}</strong>📋 Réservations</div>
          <div className="stat-card stat-yellow"><strong>{dashboard.confirmedBookings}</strong>✅ Confirmées</div>
          <div className="stat-card stat-muted"><strong>{dashboard.cancelledBookings}</strong>❌ Annulées</div>
        </section>
      )}

      <section className="card" style={{ marginBottom: '2rem' }}>
        <h2>{editingId ? 'Modifier une activité' : 'Créer une activité'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))' }}>
            <div className="form-group">
              <label>Titre</label>
              <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Lieu</label>
              <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Date et heure</label>
              <input
                type="datetime-local"
                value={form.startDateTime}
                onChange={(e) => setForm({ ...form, startDateTime: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Date limite d'inscription</label>
              <input
                type="datetime-local"
                value={form.registrationDeadline}
                onChange={(e) => setForm({ ...form, registrationDeadline: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Catalogue</label>
              <select
                value={form.catalogId}
                onChange={(e) => setForm({ ...form, catalogId: e.target.value })}
                required
              >
                <option value="">Choisir un catalogue</option>
                {catalogs.map((catalog) => (
                  <option key={catalog.id} value={catalog.id}>
                    {catalog.emoji} {catalog.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Capacité</label>
              <input
                type="number"
                min="1"
                value={form.maxCapacity}
                onChange={(e) => setForm({ ...form, maxCapacity: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Prix (€)</label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Active</label>
              <select value={form.active ? 'true' : 'false'} onChange={(e) => setForm({ ...form, active: e.target.value === 'true' })}>
                <option value="true">Oui</option>
                <option value="false">Non</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Résumé (carte)</label>
            <textarea
              rows="2"
              maxLength={500}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Détails & programme</label>
            <textarea
              rows="6"
              maxLength={5000}
              value={form.details}
              onChange={(e) => setForm({ ...form, details: e.target.value })}
              placeholder="Déroulement, horaires, matériel fourni…"
              required
            />
          </div>
          <div className="form-group">
            <label>Prérequis</label>
            <textarea
              rows="4"
              maxLength={2000}
              value={form.prerequisites}
              onChange={(e) => setForm({ ...form, prerequisites: e.target.value })}
              placeholder="Âge, tenue, documents, niveau requis…"
              required
            />
          </div>
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button className="btn btn-primary" type="submit">
              {editingId ? 'Mettre à jour' : 'Créer'}
            </button>
            {editingId && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => { setEditingId(null); setForm(emptyActivityForm); }}
              >
                Annuler
              </button>
            )}
          </div>
        </form>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Activités</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Titre</th>
              <th>Catalogue</th>
              <th>Date</th>
              <th>Limite inscription</th>
              <th>Places</th>
              <th>Prix</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {activities.map((activity) => (
              <tr key={activity.id}>
                <td>{activity.title}</td>
                <td>{activity.catalogEmoji} {activity.catalogName}</td>
                <td>{formatDate(activity.startDateTime)}</td>
                <td>{formatDate(activity.registrationDeadline)}</td>
                <td>{activity.availableSpots}/{activity.maxCapacity}</td>
                <td>{activity.price} €</td>
                <td>
                  <button className="btn btn-secondary" onClick={() => startEdit(activity)}>Modifier</button>{' '}
                  <button className="btn btn-danger" onClick={() => handleDelete(activity.id)}>Supprimer</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Utilisateurs</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Email</th>
              <th>Rôle</th>
              <th>Inscription</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.firstName} {user.lastName}</td>
                <td>{user.email}</td>
                <td>{user.role}</td>
                <td>{formatDate(user.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {dashboard?.recentBookings?.length > 0 && (
        <section>
          <h2>Réservations récentes</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Activité</th>
                <th>Enfant</th>
                <th>Parent</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {dashboard.recentBookings.map((booking) => (
                <tr key={booking.id}>
                  <td>{booking.activityTitle}</td>
                  <td>{booking.childName} ({booking.childAge} ans)</td>
                  <td>{booking.userEmail}</td>
                  <td>{booking.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </>
  );
}
