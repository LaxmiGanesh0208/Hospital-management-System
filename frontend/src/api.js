const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export async function apiRequest(path, options = {}) {
  const token = localStorage.getItem('hospital_token');
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    const body = await response.text();
    let message = body;
    try {
      const parsed = JSON.parse(body);
      message = parsed.message || Object.values(parsed).join(', ') || body;
    } catch {
      // Keep plain-text Gateway errors readable.
    }
    throw new Error(message || `Request failed (${response.status})`);
  }
  if (response.status === 204) return null;
  if (response.headers.get('content-type')?.includes('application/pdf')) return response.blob();
  return response.json();
}

const json = (method, body) => ({ method, body: JSON.stringify(body) });

export const api = {
  login: (body) => apiRequest('/auth/login', json('POST', body)),
  staffAccounts: () => apiRequest('/api/admin/staff'),
  createStaffAccount: (body) => apiRequest('/api/admin/staff', json('POST', body)),
  register: (body) => apiRequest('/auth/register', json('POST', body)),
  profile: () => apiRequest('/auth/me'),
  updateProfile: (body) => apiRequest('/auth/me', json('PUT', body)),
  logout: () => apiRequest('/auth/logout', { method: 'POST' }),
  patientList: () => apiRequest('/api/patients'),
  medicalHistory: () => apiRequest('/api/patients/me/medical-history'),
  notifications: () => apiRequest('/api/patients/me/notifications'),
  markNotificationRead: (id) => apiRequest(`/api/patients/me/notifications/${id}/read`, { method: 'PATCH' }),

  doctors: (params = {}) => apiRequest(`/api/care/doctors?${new URLSearchParams(params)}`),
  upcomingDoctorAvailability: (days = 7) => apiRequest(`/api/care/doctors/availability/upcoming?days=${days}`),
  doctor: (id) => apiRequest(`/api/care/doctors/${id}`),
  doctorAvailability: (id, date) => apiRequest(`/api/care/doctors/${id}/availability?date=${encodeURIComponent(date)}`),
  addDoctor: (body) => apiRequest('/api/care/doctors', json('POST', body)),
  addAvailability: (id, body) => apiRequest(`/api/care/doctors/${id}/availability`, json('POST', body)),
  appointments: () => apiRequest('/api/care/appointments/my'),
  allAppointments: () => apiRequest('/api/care/appointments'),
  doctorAppointments: () => apiRequest('/api/care/appointments/doctor'),
  doctorPatients: () => apiRequest('/api/care/doctor-patients'),
  addDoctorPatient: (body) => apiRequest('/api/care/doctor-patients', json('POST', body)),
  doctorPatientReviewQueue: () => apiRequest('/api/care/doctor-patients/review-queue'),
  reviewDoctorPatient: (id, body) => apiRequest(`/api/care/doctor-patients/${id}/review`, json('PATCH', body)),
  bookAppointment: (body) => apiRequest('/api/care/appointments', json('POST', body)),
  cancelAppointment: (id) => apiRequest(`/api/care/appointments/${id}/cancel`, { method: 'PATCH' }),
  careSummary: () => apiRequest('/api/care/admin/summary'),

  labTests: () => apiRequest('/api/care/lab-tests'),
  labTest: (id) => apiRequest(`/api/care/lab-tests/${id}`),
  labAvailability: (id, date) => apiRequest(`/api/care/lab-tests/${id}/availability?date=${encodeURIComponent(date)}`),
  addLabTest: (body) => apiRequest('/api/care/lab-tests', json('POST', body)),
  labReviewQueue: () => apiRequest('/api/care/lab-tests/review-queue'),
  reviewLabTest: (id, body) => apiRequest(`/api/care/lab-tests/${id}/review`, json('PATCH', body)),
  labResultReviewQueue: () => apiRequest('/api/care/lab-bookings/review-queue'),
  reviewLabResult: (id, body) => apiRequest(`/api/care/lab-bookings/${id}/review`, json('PATCH', body)),
  bookLabTest: (body) => apiRequest('/api/care/lab-bookings', json('POST', body)),
  labBookings: () => apiRequest('/api/care/lab-bookings/my'),
  allLabBookings: () => apiRequest('/api/care/lab-bookings'),
  publishLabResult: (id, body) => apiRequest(`/api/care/lab-bookings/${id}/result`, json('PATCH', body)),

  medications: (params = {}) => apiRequest(`/api/pharmacy/medications?${new URLSearchParams(params)}`),
  addMedication: (body) => apiRequest('/api/pharmacy/medications', json('POST', body)),
  pharmacyReviewQueue: () => apiRequest('/api/pharmacy/medications/review-queue'),
  reviewMedication: (id, body) => apiRequest(`/api/pharmacy/medications/${id}/review`, json('PATCH', body)),
  prescriptions: (patientId) => apiRequest(`/api/pharmacy/prescriptions/patient/${patientId}`),
  issuePrescription: (body) => apiRequest('/api/pharmacy/prescriptions', json('POST', body)),
  orders: () => apiRequest('/api/pharmacy/orders/my'),
  allOrders: () => apiRequest('/api/pharmacy/orders'),
  placeOrder: (body) => apiRequest('/api/pharmacy/orders', json('POST', body)),
  updateOrderStatus: (id, status) => apiRequest(`/api/pharmacy/orders/${id}/status`, json('PATCH', { status })),

  invoices: () => apiRequest('/api/billing/invoices/my'),
  allInvoices: () => apiRequest('/api/billing/invoices'),
  invoice: (id) => apiRequest(`/api/billing/invoices/${id}`),
  invoicePdf: (id) => apiRequest(`/api/billing/invoices/${id}/download`),
  paymentOptions: () => apiRequest('/api/billing/payment-options'),
  aiDashboard: () => apiRequest('/api/ai/dashboard'),
  aiInsights: () => apiRequest('/api/ai/insights'),
  aiPatientProfiles: () => apiRequest('/api/ai/patient-profiles'),
  aiDemoMonitoring: () => apiRequest('/api/ai/demo-monitoring'),
};
