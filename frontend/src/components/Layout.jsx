import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from './Navbar';

export default function Layout() {
  return (
    <div className="app-shell">
      <div className="blob blob-1" aria-hidden="true" />
      <div className="blob blob-2" aria-hidden="true" />
      <div className="blob blob-3" aria-hidden="true" />
      <Navbar />
      <main className="container page">
        <Outlet />
      </main>
      <footer className="site-footer">
        <div className="container footer-inner">
          <span>🎈 Kids Activities</span>
          <span>Des aventures fun pour vos enfants</span>
        </div>
      </footer>
    </div>
  );
}

export function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading, isAdmin } = useAuth();

  if (loading) {
    return <div className="empty-state">⏳ Chargement...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
}
