import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">Kids Activities</Link>
        <nav className="nav-links">
          <NavLink to="/" end>Activités</NavLink>
          {user && <NavLink to="/bookings">Mes réservations</NavLink>}
          {isAdmin && <NavLink to="/admin">Administration</NavLink>}
          {!user ? (
            <>
              <NavLink to="/login">Connexion</NavLink>
              <NavLink to="/register">Inscription</NavLink>
            </>
          ) : (
            <>
              <span>{user.firstName} {user.lastName}</span>
              <button type="button" onClick={() => { logout(); navigate('/'); }}>
                Déconnexion
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
