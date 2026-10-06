import React, { useCallback, useEffect, useState } from 'react';
import { Activity, CalendarDays, CreditCard, FileText, Pill, Receipt, Save, TestTube2 } from 'lucide-react';
import { api } from '../api';

export function PatientDashboard({ setActiveTab }) {
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [orders, setOrders] = useState([]);
  const [labBookings, setLabBookings] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [medicalHistory, setMedicalHistory] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [busy, setBusy] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const refresh = useCallback(async () => {
    setBusy(true); setError('');
    try {
      const [account, visits, rx, pharmacyOrders, tests, bills, history, notices] = await Promise.all([
        api.profile(), api.appointments(), api.prescriptions((JSON.parse(localStorage.getItem('hospital_user') || '{}')).id),
        api.orders(), api.labBookings(), api.invoices(), api.medicalHistory(), api.notifications(),
      ]);
      setProfile(account); setAppointments(visits); setPrescriptions(rx); setOrders(pharmacyOrders); setLabBookings(tests); setInvoices(bills); setMedicalHistory(history); setNotifications(notices);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }, []);
  useEffect(() => { refresh(); }, [refresh]);

  const saveProfile = async (event) => {
    event.preventDefault(); setSaving(true); setError(''); setNotice('');
    try { setProfile(await api.updateProfile({ fullName: profile.fullName, mobile: profile.mobile, address: profile.address, emergencyContact: profile.emergencyContact })); setNotice('Profile updated.'); }
    catch (err) { setError(err.message); }
    finally { setSaving(false); }
  };
  const cancelAppointment = async (id) => {
    setError('');
    try { await api.cancelAppointment(id); await refresh(); }
    catch (err) { setError(err.message); }
  };
  const markRead = async (id) => { try { await api.markNotificationRead(id); await refresh(); } catch (err) { setError(err.message); } };

  const outstanding = invoices.filter((invoice) => invoice.status === 'UNPAID').reduce((sum, item) => sum + Number(item.amount), 0);

  return <section className="page-section dashboard-section"><div className="container">
    <div className="page-heading"><span className="eyebrow">Patient portal</span><h1>{profile?.fullName ? `Hello, ${profile.fullName.split(' ')[0]}` : 'Your health overview'}</h1><p>Your appointments, prescriptions, test results, pharmacy orders, and invoices in one place.</p></div>
    {error && <p className="notice notice-error" role="alert">{error}</p>}{notice && <p className="notice notice-success">{notice}</p>}{busy && <p className="notice">Loading your account…</p>}
    {!busy && profile && <>
      <div className="dashboard-metrics"><button onClick={() => setActiveTab('doctors')}><CalendarDays size={20} /><span>Appointments</span><strong>{appointments.filter((item) => item.status === 'CONFIRMED').length}</strong></button><button onClick={() => setActiveTab('pharmacy')}><Pill size={20} /><span>Prescriptions</span><strong>{prescriptions.length}</strong></button><button onClick={() => setActiveTab('laboratory')}><TestTube2 size={20} /><span>Lab bookings</span><strong>{labBookings.length}</strong></button><button onClick={() => setActiveTab('billing')}><CreditCard size={20} /><span>Outstanding</span><strong>₹{outstanding.toFixed(2)}</strong></button></div>
      <div className="dashboard-columns"><div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Next visits</span><h2>Appointments</h2></div><button className="text-button" onClick={() => setActiveTab('doctors')}>Find a doctor</button></div>{appointments.length ? appointments.slice(0, 4).map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.doctorName}</strong><span>{item.date} · {item.startTime?.slice(0, 5)}</span></div><span className="status-pill">{item.status}</span>{item.status === 'CONFIRMED' && <button className="text-button danger-text" onClick={() => cancelAppointment(item.id)}>Cancel</button>}</article>) : <p className="muted">No appointments booked.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Account</span><h2>My profile</h2></div></div><form className="profile-form" onSubmit={saveProfile}><label>Full name<input value={profile.fullName || ''} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} required /></label><label>Email<input value={profile.email || ''} readOnly /></label><label>Mobile<input type="tel" value={profile.mobile || ''} onChange={(e) => setProfile({ ...profile, mobile: e.target.value })} required /></label><label>Address<textarea rows="2" value={profile.address || ''} onChange={(e) => setProfile({ ...profile, address: e.target.value })} required /></label><label>Emergency contact<input type="tel" value={profile.emergencyContact || ''} onChange={(e) => setProfile({ ...profile, emergencyContact: e.target.value })} required /></label><button className="btn btn-primary" disabled={saving}><Save size={16} />{saving ? 'Saving…' : 'Save profile'}</button></form></div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Medication</span><h2>Prescriptions</h2></div><button className="text-button" onClick={() => setActiveTab('pharmacy')}>Pharmacy</button></div>{prescriptions.length ? prescriptions.slice(0, 5).map((rx) => <article className="dashboard-row" key={rx.id}><div><strong>{rx.medicationName}</strong><span>{rx.dosage} · Qty {rx.quantity}</span></div><span className="status-pill">{rx.status}</span></article>) : <p className="muted">No prescriptions on file.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Diagnostics</span><h2>Lab results</h2></div><button className="text-button" onClick={() => setActiveTab('laboratory')}>Browse tests</button></div>{labBookings.length ? labBookings.slice(0, 5).map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.testName}</strong><span>{item.date} · {item.time?.slice(0, 5)}</span>{item.resultSummary && <p>{item.resultSummary}</p>}</div><span className="status-pill">{item.status}</span></article>) : <p className="muted">No laboratory bookings.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Pharmacy</span><h2>Orders</h2></div><button className="text-button" onClick={() => setActiveTab('pharmacy')}>View pharmacy</button></div>{orders.length ? orders.slice(0, 5).map((order) => <article className="dashboard-row" key={order.id}><div><strong>Order {order.id.slice(0, 8)}</strong><span>{order.items?.map((item) => `${item.medicationName} × ${item.quantity}`).join(', ')}</span></div><span className="status-pill">{order.status.replaceAll('_', ' ')}</span></article>) : <p className="muted">No pharmacy orders.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Billing</span><h2>Recent invoices</h2></div><button className="text-button" onClick={() => setActiveTab('billing')}>View billing</button></div>{invoices.length ? invoices.slice(0, 4).map((invoice) => <article className="dashboard-row" key={invoice.id}><div><strong>{invoice.description}</strong><span>{new Date(invoice.createdAt).toLocaleDateString()}</span></div><strong>₹{Number(invoice.amount).toFixed(2)}</strong></article>) : <p className="muted">No invoices yet.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Health records</span><h2>Medical history</h2></div></div>{medicalHistory.length ? medicalHistory.slice(0, 5).map((record) => <article className="dashboard-row" key={record.id}><div><strong>{record.title}</strong><span>{record.recordType} · {new Date(record.recordedAt).toLocaleDateString()} · {record.provider || 'Care team'}</span><p>{record.details}</p></div></article>) : <p className="muted">No history records on file.</p>}</div>
        <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Updates</span><h2>Notifications</h2></div></div>{notifications.length ? notifications.slice(0, 6).map((notice) => <article className="dashboard-row" key={notice.id}><div><strong>{notice.title}</strong><span>{notice.type} · {notice.message}</span></div>{!notice.read && <button className="text-button" onClick={() => markRead(notice.id)}>Mark read</button>}</article>) : <p className="muted">No notifications.</p>}</div>
      </div>
    </>}
  </div></section>;
}
