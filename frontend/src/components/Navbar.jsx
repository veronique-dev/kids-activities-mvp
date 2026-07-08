import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">
          <span className="brand-icon" aria-hidden="true">🎨</span>
          <span className="brand-text">
            Kids<span className="brand-accent">Activities</span>
          </span>
        </Link>
        <nav className="nav-links">
          <NavLink to="/" end className="nav-pill">🏠 Activités</NavLink>
          {user && <NavLink to="/bookings" className="nav-pill">📋 Réservations</NavLink>}
          {isAdmin && <NavLink to="/admin" className="nav-pill">⚙️ Admin</NavLink>}
          {!user ? (
            <>
              <NavLink to="/login" className="nav-pill">Connexion</NavLink>
              <NavLink to="/register" className="btn btn-accent btn-sm">S'inscrire ✨</NavLink>
            </>
          ) : (
            <div className="nav-user">
              <span className="user-avatar" aria-hidden="true">
                {user.firstName?.charAt(0) || '?'}
              </span>
              <span className="user-name">{user.firstName}</span>
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => { logout(); navigate('/'); }}
              >
                Déconnexion
              </button>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
