const API_BASE = import.meta.env.VITE_API_URL || '/api';

function getToken() {
  return localStorage.getItem('token');
}

async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401 && getToken()) {
      localStorage.removeItem('token');
    }
    const error = await response.json().catch(() => ({ message: 'Erreur réseau' }));
    throw new Error(error.message || 'Une erreur est survenue');
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  getMe: () => request('/users/me'),
  getActivities: (catalogId) => {
    const query = catalogId ? `?catalogId=${catalogId}` : '';
    return request(`/activities${query}`);
  },
  getCatalogs: () => request('/catalogs'),
  getAllCatalogs: () => request('/catalogs/admin/all'),
  getAllActivities: () => request('/activities/admin/all'),
  createActivity: (data) => request('/activities', { method: 'POST', body: JSON.stringify(data) }),
  updateActivity: (id, data) => request(`/activities/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteActivity: (id) => request(`/activities/${id}`, { method: 'DELETE' }),
  getBookings: () => request('/bookings'),
  createBooking: (data) => request('/bookings', { method: 'POST', body: JSON.stringify(data) }),
  cancelBooking: (id) => request(`/bookings/${id}`, { method: 'DELETE' }),
  createPaymentCheckout: (bookingId) =>
    request('/payments/checkout', { method: 'POST', body: JSON.stringify({ bookingId }) }),
  completeMockPayment: (paymentId) =>
    request(`/payments/${paymentId}/complete-mock`, { method: 'POST' }),
  verifyStripeSession: (sessionId) =>
    request(`/payments/verify-session?sessionId=${encodeURIComponent(sessionId)}`, { method: 'POST' }),
  getPayment: (id) => request(`/payments/${id}`),
  getDashboard: () => request('/admin/dashboard'),
  getUsers: () => request('/users'),
};
