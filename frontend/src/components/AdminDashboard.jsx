import React, { useEffect, useState } from 'react';
import { Activity, AlertTriangle, CalendarDays, FlaskConical, Pill, Plus, Receipt, RefreshCw, ShieldCheck, Stethoscope, Users } from 'lucide-react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';

const today = () => { const date = new Date(); date.setHours(12, 0, 0, 0); return date.toISOString().slice(0, 10); };
const sectionItems = [
  ['overview', 'Overview', Activity], ['patients', 'Patients', Users], ['doctors', 'Doctors', Stethoscope],
  ['appointments', 'Appointments', CalendarDays], ['staff', 'Department access', Users], ['pharmacy', 'Pharmacy', Pill],
  ['laboratory', 'Laboratory', FlaskConical], ['billing', 'Billing', Receipt], ['ai', 'AI insights', ShieldCheck],
];

export function AdminDashboard() {
  const { user, isAdmin } = useAuth();
  const [section, setSection] = useState('overview');
  const [data, setData] = useState({});
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [doctorForm, setDoctorForm] = useState({ name: '', specialization: '', qualification: '', yearsExperience: '', consultationFee: '', bio: '' });
  const [availabilityForm, setAvailabilityForm] = useState({ doctorId: '', date: today(), startTime: '', endTime: '' });
  const [testForm, setTestForm] = useState({ code: '', name: '', category: '', description: '', price: '', fastingRequired: false });
  const [medicineForm, setMedicineForm] = useState({ name: '', code: '', stockQuantity: '', reorderThreshold: '', price: '', category: '', description: '', requiresPrescription: false });
  const [staffForm, setStaffForm] = useState({ fullName: '', email: '', password: '', role: 'PHARMACIST', doctorId: '' });
  const [assignmentForm, setAssignmentForm] = useState({ doctorId: '', patientEmail: '', note: '' });

  const refresh = async () => {
    setBusy(true); setErrors({});
    const jobs = {
      summary: api.careSummary(), doctors: api.doctors(), appointments: api.allAppointments(), patients: api.patientList(),
      labBookings: api.allLabBookings(), labTests: api.labTests(), orders: api.allOrders(),
      medications: api.medications(), invoices: api.allInvoices(), ai: api.aiDashboard(), insights: api.aiInsights(),
      staff: api.staffAccounts(),
    };
    const results = await Promise.all(Object.entries(jobs).map(async ([key, promise]) => {
      try { return [key, await promise, null]; } catch (error) { return [key, null, error.message]; }
    }));
    const next = {}; const nextErrors = {};
    for (const [key, value, error] of results) { if (error) nextErrors[key] = error; else next[key] = value; }
    setData(next); setErrors(nextErrors); setBusy(false);
  };
  useEffect(() => { if (isAdmin) refresh(); }, [isAdmin]);

  const runAction = async (action, successMessage) => {
    setBusy(true); setMessage(''); setErrors({});
    try { await action(); setMessage(successMessage); await refresh(); }
    catch (error) { setErrors({ action: error.message }); setBusy(false); }
  };
  const addDoctor = (event) => {
    event.preventDefault();
    runAction(async () => {
      const doctor = await api.addDoctor({ ...doctorForm, yearsExperience: Number(doctorForm.yearsExperience), consultationFee: Number(doctorForm.consultationFee), active: true });
      setAvailabilityForm((form) => ({ ...form, doctorId: doctor.id }));
      setDoctorForm({ name: '', specialization: '', qualification: '', yearsExperience: '', consultationFee: '', bio: '' });
    }, 'Doctor added. Add appointment availability below.');
  };
  const addAvailability = (event) => { event.preventDefault(); runAction(() => api.addAvailability(availabilityForm.doctorId, { date: availabilityForm.date, startTime: availabilityForm.startTime, endTime: availabilityForm.endTime }), 'Appointment slot added.'); };
  const addTest = (event) => { event.preventDefault(); runAction(async () => { await api.addLabTest({ ...testForm, price: Number(testForm.price), active: true }); setTestForm({ code: '', name: '', category: '', description: '', price: '', fastingRequired: false }); }, 'Laboratory test submitted for a second staff review.'); };
  const addMedication = (event) => { event.preventDefault(); runAction(async () => { await api.addMedication({ ...medicineForm, stockQuantity: Number(medicineForm.stockQuantity), reorderThreshold: Number(medicineForm.reorderThreshold), price: Number(medicineForm.price) }); setMedicineForm({ name: '', code: '', stockQuantity: '', reorderThreshold: '', price: '', category: '', description: '', requiresPrescription: false }); }, 'Medication submitted for a second staff review.'); };
  const addStaff = (event) => { event.preventDefault(); runAction(async () => { await api.createStaffAccount({ ...staffForm, doctorId: staffForm.doctorId || null }); setStaffForm({ fullName: '', email: '', password: '', role: 'PHARMACIST', doctorId: '' }); }, 'Department login created. Share the credentials securely with that staff member.'); };
  const assignPatient = (event) => { event.preventDefault(); runAction(async () => { await api.addDoctorPatient(assignmentForm); setAssignmentForm({ doctorId: '', patientEmail: '', note: '' }); }, 'Patient assignment submitted for a second staff review.'); };

  if (!isAdmin) return <section className="page-section"><div className="container"><div className="empty-state"><AlertTriangle size={30} /><h2>Administration access required</h2><p>Your signed-in account does not have an administrator role.</p></div></div></section>;

  const metric = (label, value, Icon) => <article className="admin-metric"><span>{React.createElement(Icon, { size: 18 })}</span><small>{label}</small><strong>{value ?? '—'}</strong></article>;
  const listError = (key) => errors[key] && <p className="notice notice-error">{key}: {errors[key]}</p>;

  return <section className="admin-shell"><aside className="admin-sidebar"><div className="admin-identity"><ShieldCheck size={21} /><span><strong>Administration</strong><small>{user?.email}</small></span></div><nav>{sectionItems.map(([id, label, Icon]) => <button key={id} className={section === id ? 'active' : ''} onClick={() => setSection(id)}>{React.createElement(Icon, { size: 17 })}{label}</button>)}</nav><button className="admin-refresh" onClick={refresh} disabled={busy}><RefreshCw size={16} className={busy ? 'spin' : ''} />Refresh data</button></aside>
    <div className="admin-content"><header className="admin-heading"><div><span className="eyebrow">Hospital operations</span><h1>{sectionItems.find(([id]) => id === section)?.[1]}</h1></div><span className="role-pill">{user?.role}</span></header>
      {message && <p className="notice notice-success">{message}</p>}{errors.action && <p className="notice notice-error" role="alert">{errors.action}</p>}
      {section === 'overview' && <><div className="admin-metrics">{metric('Patients', data.patients?.length, Users)}{metric('Appointments today', data.summary?.appointmentsToday, CalendarDays)}{metric('Active doctors', data.summary?.activeDoctors, Stethoscope)}{metric('Laboratory bookings today', data.summary?.labBookingsToday, FlaskConical)}{metric('Outstanding invoices', data.invoices?.filter((item) => item.status === 'UNPAID').length, Receipt)}{metric('AI high-risk profiles', data.ai?.highRiskPatients, AlertTriangle)}</div><div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Monitoring</span><h2>Recent AI insights</h2></div></div>{listError('ai')}{data.insights?.slice(0, 6).map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.insightType}</strong><span>{item.message}</span></div><span className="status-pill">{item.severity}</span></article>)}</div></>}
      {section === 'patients' && <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Patient records</span><h2>Patients</h2></div></div>{listError('patients')}{data.patients?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.email} · {item.mobile || 'No mobile'} · {item.dateOfBirth}</span></div><span>{item.id.slice(0, 8)}</span></article>)}</div>}
      {section === 'doctors' && <div className="admin-columns"><div className="dashboard-panel"><h2>Add doctor</h2><form className="profile-form" onSubmit={addDoctor}><label>Name<input required value={doctorForm.name} onChange={(e) => setDoctorForm({ ...doctorForm, name: e.target.value })} /></label><label>Specialization<input required value={doctorForm.specialization} onChange={(e) => setDoctorForm({ ...doctorForm, specialization: e.target.value })} /></label><label>Qualifications<input value={doctorForm.qualification} onChange={(e) => setDoctorForm({ ...doctorForm, qualification: e.target.value })} /></label><div className="form-two"><label>Experience (years)<input type="number" min="0" required value={doctorForm.yearsExperience} onChange={(e) => setDoctorForm({ ...doctorForm, yearsExperience: e.target.value })} /></label><label>Fee (₹)<input type="number" min="0" step="0.01" required value={doctorForm.consultationFee} onChange={(e) => setDoctorForm({ ...doctorForm, consultationFee: e.target.value })} /></label></div><label>Profile description<textarea rows="3" value={doctorForm.bio} onChange={(e) => setDoctorForm({ ...doctorForm, bio: e.target.value })} /></label><button className="btn btn-primary" disabled={busy}><Plus size={16} />Add doctor</button></form></div><div className="dashboard-panel"><h2>Add appointment slot</h2><form className="profile-form" onSubmit={addAvailability}><label>Doctor<select required value={availabilityForm.doctorId} onChange={(e) => setAvailabilityForm({ ...availabilityForm, doctorId: e.target.value })}><option value="">Choose doctor</option>{data.doctors?.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Date<input type="date" min={today()} required value={availabilityForm.date} onChange={(e) => setAvailabilityForm({ ...availabilityForm, date: e.target.value })} /></label><div className="form-two"><label>Start time<input type="time" required value={availabilityForm.startTime} onChange={(e) => setAvailabilityForm({ ...availabilityForm, startTime: e.target.value })} /></label><label>End time<input type="time" required value={availabilityForm.endTime} onChange={(e) => setAvailabilityForm({ ...availabilityForm, endTime: e.target.value })} /></label></div><button className="btn btn-primary" disabled={busy || !availabilityForm.doctorId}><Plus size={16} />Add time slot</button></form>{data.doctors?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.specialization} · ₹{item.consultationFee}</span></div><span>{item.yearsExperience} yrs</span></article>)}</div></div>}
      {section === 'appointments' && <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Schedule</span><h2>Appointments</h2></div></div>{listError('appointments')}{data.appointments?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.doctorName}</strong><span>{item.patientEmail} · {item.date} {item.startTime?.slice(0, 5)}</span></div><span className="status-pill">{item.status}</span>{item.status === 'CONFIRMED' && <button className="text-button danger-text" onClick={() => runAction(() => api.cancelAppointment(item.id), 'Appointment cancelled.')}>Cancel</button>}</article>)}</div>}
      {section === 'staff' && <div className="admin-columns"><div className="dashboard-panel"><h2>Create department login</h2><p className="muted small">Each staff member signs in with their own email and password. Assign a separate reviewer account for each department.</p><form className="profile-form" onSubmit={addStaff}><label>Name<input required value={staffForm.fullName} onChange={(e) => setStaffForm({ ...staffForm, fullName: e.target.value })} /></label><label>Email<input type="email" required value={staffForm.email} onChange={(e) => setStaffForm({ ...staffForm, email: e.target.value })} /></label><label>Temporary password<input type="password" minLength="8" maxLength="72" required value={staffForm.password} onChange={(e) => setStaffForm({ ...staffForm, password: e.target.value })} /></label><label>Department role<select value={staffForm.role} onChange={(e) => setStaffForm({ ...staffForm, role: e.target.value, doctorId: '' })}><option value="PHARMACIST">Pharmacy staff</option><option value="PHARMACY_REVIEWER">Pharmacy co-helper reviewer</option><option value="LAB_TECH">Laboratory staff</option><option value="LAB_REVIEWER">Laboratory co-helper reviewer</option><option value="DOCTOR">Doctor</option><option value="DOCTOR_REVIEWER">Doctor co-helper reviewer</option></select></label>{staffForm.role.startsWith('DOCTOR') && <label>Doctor profile<select required value={staffForm.doctorId} onChange={(e) => setStaffForm({ ...staffForm, doctorId: e.target.value })}><option value="">Choose doctor</option>{data.doctors?.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name} · {doctor.specialization}</option>)}</select></label>}<button className="btn btn-primary" disabled={busy}><Plus size={16} />Create staff login</button></form></div><div className="dashboard-panel"><h2>Department accounts</h2>{data.staff?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.fullName}</strong><span>{item.email}{item.doctorId ? ` · Doctor profile linked` : ''}</span></div><span className="status-pill">{item.role}</span></article>)}<h2 className="section-subheading">Assign patient to doctor</h2><p className="muted small">A second doctor co-helper must review this assignment before it appears in the doctor workspace.</p><form className="profile-form" onSubmit={assignPatient}><label>Doctor<select required value={assignmentForm.doctorId} onChange={(e) => setAssignmentForm({ ...assignmentForm, doctorId: e.target.value })}><option value="">Choose doctor</option>{data.doctors?.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name}</option>)}</select></label><label>Patient<select required value={assignmentForm.patientEmail} onChange={(e) => setAssignmentForm({ ...assignmentForm, patientEmail: e.target.value })}><option value="">Choose patient</option>{data.patients?.map((patient) => <option key={patient.id} value={patient.email}>{patient.name} · {patient.email}</option>)}</select></label><label>Assignment note<textarea rows="2" value={assignmentForm.note} onChange={(e) => setAssignmentForm({ ...assignmentForm, note: e.target.value })} /></label><button className="btn btn-secondary" disabled={busy}>Submit for review</button></form></div></div>}
      {section === 'pharmacy' && <div className="admin-columns"><div className="dashboard-panel"><h2>Add medication</h2><form className="profile-form" onSubmit={addMedication}><label>Name<input required value={medicineForm.name} onChange={(e) => setMedicineForm({ ...medicineForm, name: e.target.value })} /></label><label>Code<input required value={medicineForm.code} onChange={(e) => setMedicineForm({ ...medicineForm, code: e.target.value })} /></label><label>Category<input required value={medicineForm.category} onChange={(e) => setMedicineForm({ ...medicineForm, category: e.target.value })} /></label><div className="form-two"><label>Stock<input type="number" min="0" required value={medicineForm.stockQuantity} onChange={(e) => setMedicineForm({ ...medicineForm, stockQuantity: e.target.value })} /></label><label>Reorder alert at<input type="number" min="0" required value={medicineForm.reorderThreshold} onChange={(e) => setMedicineForm({ ...medicineForm, reorderThreshold: e.target.value })} /></label></div><label>Price (₹)<input type="number" min="0" step="0.01" required value={medicineForm.price} onChange={(e) => setMedicineForm({ ...medicineForm, price: e.target.value })} /></label><label>Description<textarea value={medicineForm.description} onChange={(e) => setMedicineForm({ ...medicineForm, description: e.target.value })} /></label><label className="check-label"><input type="checkbox" checked={medicineForm.requiresPrescription} onChange={(e) => setMedicineForm({ ...medicineForm, requiresPrescription: e.target.checked })} />Requires prescription</label><button className="btn btn-primary" disabled={busy}><Plus size={16} />Add to inventory</button></form></div><div className="dashboard-panel"><h2>Inventory</h2>{data.medications?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.category} · {item.code}</span></div><span className={item.stockQuantity <= item.reorderThreshold ? 'status-pill status-alert' : 'status-pill'}>{item.stockQuantity} in stock</span></article>)}</div><div className="dashboard-panel"><h2>Orders</h2>{data.orders?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.patientEmail}</strong><span>₹{item.total} · {item.status}</span></div>{item.status === 'PAID' && <button className="text-button" onClick={() => runAction(() => api.updateOrderStatus(item.id, 'PROCESSING'), 'Order moved to processing.')}>Process</button>}</article>)}</div></div>}
      {section === 'laboratory' && <div className="admin-columns"><div className="dashboard-panel"><h2>Add test</h2><p className="muted small">New tests remain hidden until a different lab reviewer approves them.</p><form className="profile-form" onSubmit={addTest}><label>Test code<input required value={testForm.code} onChange={(e) => setTestForm({ ...testForm, code: e.target.value })} /></label><label>Test name<input required value={testForm.name} onChange={(e) => setTestForm({ ...testForm, name: e.target.value })} /></label><label>Category<input required value={testForm.category} onChange={(e) => setTestForm({ ...testForm, category: e.target.value })} /></label><label>Price (₹)<input type="number" min="0" step="0.01" required value={testForm.price} onChange={(e) => setTestForm({ ...testForm, price: e.target.value })} /></label><label>Description<textarea rows="3" value={testForm.description} onChange={(e) => setTestForm({ ...testForm, description: e.target.value })} /></label><label className="check-label"><input type="checkbox" checked={testForm.fastingRequired} onChange={(e) => setTestForm({ ...testForm, fastingRequired: e.target.checked })} />Fasting required</label><button className="btn btn-primary" disabled={busy}><Plus size={16} />Submit for review</button></form>{data.labTests?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.category} · ₹{item.price}</span></div></article>)}</div><div className="dashboard-panel"><h2>Bookings & result drafts</h2><p className="muted small">Published results require a second lab staff review.</p>{data.labBookings?.map((item) => <LabResultRow key={item.id} item={item} runAction={runAction} />)}</div></div>}
      {section === 'billing' && <div className="dashboard-panel"><div className="panel-heading"><div><span className="eyebrow">Accounts</span><h2>Invoices</h2></div></div>{listError('invoices')}{data.invoices?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.description}</strong><span>{item.patientEmail} · {item.referenceType} · {item.currency} {item.amount}</span></div><span className="status-pill">{item.status}</span></article>)}</div>}
      {section === 'ai' && <><DemoMonitoringPanel /><div className="admin-columns"><div className="dashboard-panel"><h2>AI metrics</h2>{listError('ai')}{Object.entries(data.ai || {}).map(([key, value]) => <article className="dashboard-row" key={key}><strong>{key.replaceAll(/([A-Z])/g, ' $1')}</strong><span>{String(value)}</span></article>)}</div><div className="dashboard-panel"><h2>Safety insights</h2>{data.insights?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.insightType}</strong><span>{item.message}</span></div><span className="status-pill">{item.severity}</span></article>)}</div></div></>}
    </div>
  </section>;
}

function DemoMonitoringPanel() {
  const [monitoring, setMonitoring] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const result = await api.aiDemoMonitoring();
        if (active) { setMonitoring(result); setError(''); }
      } catch (err) { if (active) setError(err.message); }
    };
    load();
    const timer = window.setInterval(load, 5000);
    return () => { active = false; window.clearInterval(timer); };
  }, []);

  return <section className="dashboard-panel" aria-label="Demo patient monitoring">
    <div className="panel-heading"><div><span className="eyebrow">SIMULATED DEMO</span><h2>Patient monitoring</h2></div><span className="status-pill">No device connected</span></div>
    <p className="muted small">{monitoring?.notice || 'Loading clearly labeled synthetic readings…'}</p>
    {error && <p className="notice notice-error" role="alert">Demo monitoring: {error}</p>}
    {!monitoring?.enabled && !error && <p className="notice">Demo stream is disabled. Enable AI_DEMO_MONITORING_ENABLED in Compose to display synthetic data.</p>}
    {monitoring?.readings?.map((reading) => <article className="dashboard-row" key={reading.patientId}>
      <div><strong>{reading.patientName} · {reading.location}</strong><span>{reading.patientId} · HR {reading.heartRateBpm} bpm · SpO₂ {reading.oxygenSaturationPercent}%</span></div>
      <span className="status-pill">{reading.status}</span>
    </article>)}
    <h3 className="monitor-alert-heading">Demo staff alerts</h3>
    {monitoring?.alerts?.length ? monitoring.alerts.map((alert) => <article className="dashboard-row" key={alert.id}>
      <div><strong>{alert.severity} · {alert.patientName}</strong><span>{alert.message}</span><small>{alert.delivery}</small></div>
      <span className="status-pill status-alert">{alert.department}</span>
    </article>) : <p className="muted small">A synthetic alert will appear after the demo stream has run for about 30 seconds.</p>}
    <p className="muted small">{monitoring?.evaluation} Alerts appear here only; SMS and phone push are not configured.</p>
  </section>;
}

function LabResultRow({ item, runAction }) {
  const [summary, setSummary] = useState(item.resultSummary || '');
  return <article className="admin-lab-row"><div className="dashboard-row"><div><strong>{item.testName}</strong><span>{item.patientEmail} · {item.date} {item.time?.slice(0, 5)} · {item.status}</span></div></div>{['BOOKED', 'RESULT_REJECTED'].includes(item.status) && <><label>Result summary<textarea rows="2" value={summary} onChange={(e) => setSummary(e.target.value)} /></label><button className="btn btn-secondary" disabled={!summary.trim()} onClick={() => runAction(() => api.publishLabResult(item.id, { resultSummary: summary, status: 'COMPLETED' }), 'Result submitted for second staff review.')}>Send for review</button></>}</article>;
}
