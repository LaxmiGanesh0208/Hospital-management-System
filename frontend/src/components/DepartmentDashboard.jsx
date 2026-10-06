import React, { useCallback, useEffect, useState } from 'react';
import { FlaskConical, Pill, RefreshCw, ShieldCheck, Stethoscope } from 'lucide-react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';

const roleNames = {
  PHARMACIST: 'Pharmacy', PHARMACY_REVIEWER: 'Pharmacy review',
  LAB_TECH: 'Laboratory', LAB_REVIEWER: 'Laboratory review',
  DOCTOR: 'Doctor workspace', DOCTOR_REVIEWER: 'Doctor review',
};
const blankMedicine = { name: '', code: '', category: '', stockQuantity: '', reorderThreshold: '', price: '', description: '', requiresPrescription: false };
const blankTest = { code: '', name: '', category: '', description: '', price: '', fastingRequired: false, active: true };

export function DepartmentDashboard() {
  const { user, role } = useAuth();
  const [data, setData] = useState({});
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [medicine, setMedicine] = useState(blankMedicine);
  const [test, setTest] = useState(blankTest);

  const refresh = useCallback(async () => {
    setBusy(true); setError('');
    const jobs = role.startsWith('PHARMACY')
      ? { medications: api.medications(), ...(role === 'PHARMACY_REVIEWER' ? { review: api.pharmacyReviewQueue() } : { orders: api.allOrders() }) }
      : role.startsWith('LAB_')
        ? { bookings: api.allLabBookings(), tests: api.labTests(), ...(role === 'LAB_REVIEWER' ? { review: api.labReviewQueue(), resultReview: api.labResultReviewQueue() } : {}) }
        : { appointments: api.doctorAppointments(), patients: api.doctorPatients(), ...(role === 'DOCTOR_REVIEWER' ? { review: api.doctorPatientReviewQueue() } : {}) };
    const results = await Promise.all(Object.entries(jobs).map(async ([key, promise]) => {
      try { return [key, await promise, null]; } catch (err) { return [key, null, err.message]; }
    }));
    const next = {}; const failures = results.filter(([, , issue]) => issue);
    for (const [key, value] of results) if (value) next[key] = value;
    setData(next); if (failures.length) setError(failures.map(([key, , issue]) => `${key}: ${issue}`).join(' · '));
    setBusy(false);
  }, [role]);
  useEffect(() => { refresh(); }, [refresh]);

  const action = async (run, success) => {
    setBusy(true); setError(''); setMessage('');
    try { await run(); setMessage(success); await refresh(); }
    catch (err) { setError(err.message); setBusy(false); }
  };
  const submitMedicine = (event) => { event.preventDefault(); action(() => api.addMedication({ ...medicine, stockQuantity: Number(medicine.stockQuantity), reorderThreshold: Number(medicine.reorderThreshold), price: Number(medicine.price) }).then(() => setMedicine(blankMedicine)), 'Medicine sent to a different pharmacy reviewer for approval.'); };
  const submitTest = (event) => { event.preventDefault(); action(() => api.addLabTest({ ...test, price: Number(test.price) }).then(() => setTest(blankTest)), 'Lab test sent to a different laboratory reviewer for approval.'); };
  const review = (kind, id, decision) => action(() => (kind === 'medicine' ? api.reviewMedication(id, { decision }) : kind === 'test' ? api.reviewLabTest(id, { decision }) : api.reviewDoctorPatient(id, { decision })), `${decision === 'APPROVED' ? 'Approved' : 'Rejected'} after review.`);

  const roleIcon = role.startsWith('PHARMACY') ? Pill : role.startsWith('LAB_') ? FlaskConical : Stethoscope;
  const Icon = roleIcon;
  return <section className="admin-shell"><aside className="admin-sidebar"><div className="admin-identity"><Icon size={21} /><span><strong>{roleNames[role]}</strong><small>{user?.email}</small></span></div><div className="dashboard-panel"><p className="eyebrow">Department access</p><p className="muted small">This account can access only its department tools. New catalogue entries stay hidden until another staff member approves them.</p></div><button className="admin-refresh" onClick={refresh} disabled={busy}><RefreshCw size={16} className={busy ? 'spin' : ''} />Refresh data</button></aside>
    <div className="admin-content"><header className="admin-heading"><div><span className="eyebrow">Department workspace</span><h1>{roleNames[role]}</h1></div><span className="role-pill">{role}</span></header>
      {message && <p className="notice notice-success">{message}</p>}{error && <p className="notice notice-error" role="alert">{error}</p>}
      {role === 'PHARMACIST' && <div className="admin-columns"><section className="dashboard-panel"><h2>Submit medicine</h2><p className="muted small">A co-helper pharmacy reviewer must approve it before patients can see or order it.</p><form className="profile-form" onSubmit={submitMedicine}><label>Name<input required value={medicine.name} onChange={(e) => setMedicine({ ...medicine, name: e.target.value })} /></label><label>Code<input required value={medicine.code} onChange={(e) => setMedicine({ ...medicine, code: e.target.value })} /></label><label>Category<input required value={medicine.category} onChange={(e) => setMedicine({ ...medicine, category: e.target.value })} /></label><div className="form-two"><label>Stock<input type="number" min="0" required value={medicine.stockQuantity} onChange={(e) => setMedicine({ ...medicine, stockQuantity: e.target.value })} /></label><label>Reorder level<input type="number" min="0" required value={medicine.reorderThreshold} onChange={(e) => setMedicine({ ...medicine, reorderThreshold: e.target.value })} /></label></div><label>Price (₹)<input type="number" min="0" step="0.01" required value={medicine.price} onChange={(e) => setMedicine({ ...medicine, price: e.target.value })} /></label><label>Description<textarea rows="2" value={medicine.description} onChange={(e) => setMedicine({ ...medicine, description: e.target.value })} /></label><label className="check-label"><input type="checkbox" checked={medicine.requiresPrescription} onChange={(e) => setMedicine({ ...medicine, requiresPrescription: e.target.checked })} />Prescription required</label><button className="btn btn-primary" disabled={busy}>Submit for review</button></form></section><section className="dashboard-panel"><h2>Published inventory</h2>{data.medications?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.category} · {item.code} · ₹{item.price}</span></div><span className="status-pill">{item.stockQuantity} stock</span></article>)}</section><section className="dashboard-panel"><h2>Orders</h2>{data.orders?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.patientEmail}</strong><span>₹{item.total}</span></div><span className="status-pill">{item.status}</span></article>)}</section></div>}
      {role === 'PHARMACY_REVIEWER' && <ReviewList title="Medicine approval queue" items={data.review} onReview={(id, decision) => review('medicine', id, decision)} render={(item) => <><strong>{item.name} · {item.code}</strong><span>{item.category} · ₹{item.price} · {item.stockQuantity} in stock</span>{item.requiresPrescription && <span>Prescription required</span>}</>} />}
      {role === 'LAB_TECH' && <div className="admin-columns"><section className="dashboard-panel"><h2>Submit a lab test</h2><p className="muted small">A different laboratory reviewer must approve it before it appears in the patient catalogue.</p><form className="profile-form" onSubmit={submitTest}><label>Test code<input required value={test.code} onChange={(e) => setTest({ ...test, code: e.target.value })} /></label><label>Test name<input required value={test.name} onChange={(e) => setTest({ ...test, name: e.target.value })} /></label><label>Category<input required value={test.category} onChange={(e) => setTest({ ...test, category: e.target.value })} /></label><label>Price (₹)<input type="number" min="0" step="0.01" required value={test.price} onChange={(e) => setTest({ ...test, price: e.target.value })} /></label><label>Description<textarea rows="2" value={test.description} onChange={(e) => setTest({ ...test, description: e.target.value })} /></label><label className="check-label"><input type="checkbox" checked={test.fastingRequired} onChange={(e) => setTest({ ...test, fastingRequired: e.target.checked })} />Fasting required</label><button className="btn btn-primary" disabled={busy}>Submit for review</button></form></section><section className="dashboard-panel"><h2>Published tests</h2>{data.tests?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.name}</strong><span>{item.category} · ₹{item.price}</span></div></article>)}</section><section className="dashboard-panel"><h2>Bookings and result entry</h2>{data.bookings?.map((item) => <LabResultEditor key={item.id} item={item} onSubmit={(body) => action(() => api.publishLabResult(item.id, body), 'Result submitted for second-person review.')} />)}</section></div>}
      {role === 'LAB_REVIEWER' && <><ReviewList title="Laboratory test approval queue" items={data.review} onReview={(id, decision) => review('test', id, decision)} render={(item) => <><strong>{item.name} · {item.code}</strong><span>{item.category} · ₹{item.price}</span><span>{item.description}</span></>} /><div className="section-gap"><ReviewList title="Lab result review queue" items={data.resultReview} onReview={(id, decision) => action(() => api.reviewLabResult(id, { decision }), `Result ${decision.toLowerCase()} after review.`)} render={(item) => <><strong>{item.testName} · {item.patientEmail}</strong><span>{item.date} · {item.time}</span><span>{item.resultSummary}</span></>} /></div></>}
      {role === 'DOCTOR' && <div className="admin-columns"><section className="dashboard-panel"><h2>My booked appointments</h2>{data.appointments?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.patientEmail}</strong><span>{item.date} · {item.startTime?.slice(0, 5)} · {item.status}</span>{item.notes && <span>{item.notes}</span>}</div></article>)}</section><section className="dashboard-panel"><h2>Manually assigned patients</h2>{data.patients?.map((item) => <article className="dashboard-row" key={item.id}><div><strong>{item.patientEmail}</strong><span>{item.note || 'No assignment note'}</span></div><span className="status-pill">Reviewed</span></article>)}</section></div>}
      {role === 'DOCTOR_REVIEWER' && <ReviewList title="Patient assignment review" items={data.review} onReview={(id, decision) => review('assignment', id, decision)} render={(item) => <><strong>{item.patientEmail} → {item.doctorName}</strong>{item.note && <span>{item.note}</span>}</>} />}
      <p className="muted small department-privacy"><ShieldCheck size={15} /> Patient data is limited to appointments for the linked doctor profile or assignments that have passed a second-person review.</p>
    </div>
  </section>;
}

function ReviewList({ title, items = [], onReview, render }) {
  return <section className="dashboard-panel"><div className="panel-heading"><h2>{title}</h2><span className="role-pill">{items.length || 0} pending</span></div>{items.length ? items.map((item) => <article className="admin-lab-row" key={item.id}><div className="review-entry">{render(item)}</div><div className="review-actions"><button className="btn btn-primary" onClick={() => onReview(item.id, 'APPROVED')}>Approve</button><button className="btn btn-secondary" onClick={() => onReview(item.id, 'REJECTED')}>Reject</button></div></article>) : <p className="muted small">Nothing is waiting for review.</p>}</section>;
}

function LabResultEditor({ item, onSubmit }) {
  const [summary, setSummary] = useState(item.resultSummary || '');
  const canSubmit = ['BOOKED', 'RESULT_REJECTED'].includes(item.status);
  return <article className="admin-lab-row"><div className="dashboard-row"><div><strong>{item.testName}</strong><span>{item.patientEmail} · {item.date} · {item.time} · {item.status}</span></div></div>{canSubmit && <><label>Result summary<textarea rows="2" value={summary} onChange={(e) => setSummary(e.target.value)} /></label><button className="btn btn-secondary" disabled={!summary.trim()} onClick={() => onSubmit({ resultSummary: summary, status: 'COMPLETED' })}>Send for co-helper review</button></>}</article>;
}
