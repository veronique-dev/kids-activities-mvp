import { Navigate, Route, Routes } from 'react-router-dom';
import Layout, { ProtectedRoute } from './components/Layout';
import ActivitiesPage from './pages/ActivitiesPage';
import BookingsPage from './pages/BookingsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminPage from './pages/AdminPage';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<ActivitiesPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route
          path="bookings"
          element={(
            <ProtectedRoute>
              <BookingsPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="admin"
          element={(
            <ProtectedRoute adminOnly>
              <AdminPage />
            </ProtectedRoute>
          )}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
