import React, { useEffect, useMemo, useState } from 'react';
import { CalendarDays, Clock3, Search, TestTube2, X } from 'lucide-react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';

const tomorrow = () => { const value = new Date(); value.setDate(value.getDate() + 1); value.setHours(12, 0, 0, 0); return value.toISOString().slice(0, 10); };

export function LaboratorySection({ openAuthModal }) {
  const { isAuthenticated } = useAuth();
  const [tests, setTests] = useState([]);
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState('');
  const [date, setDate] = useState(tomorrow());
  const [times, setTimes] = useState([]);
  const [time, setTime] = useState('');
  const [method, setMethod] = useState('IN_PERSON');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    let active = true; setLoading(true); setError('');
    api.labTests().then((result) => { if (active) setTests(result); })
      .catch((err) => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  useEffect(() => {
    if (!selected) return undefined;
    let active = true; setTimes([]); setTime(''); setError('');
    api.labAvailability(selected.id, date).then((result) => { if (active) setTimes(result); })
      .catch((err) => { if (active) setError(err.message); });
    return () => { active = false; };
  }, [selected, date]);

  const filtered = useMemo(() => tests.filter((test) => `${test.name} ${test.category} ${test.code}`.toLowerCase().includes(query.toLowerCase())), [tests, query]);
  const book = async () => {
    if (!isAuthenticated) { openAuthModal('PATIENT'); return; }
    if (!time) return;
    setSaving(true); setError('');
    try {
      const result = await api.bookLabTest({ testId: selected.id, date, time, collectionMethod: method });
      setSuccess(result); setTimes((items) => items.filter((item) => item !== time)); setTime('');
    } catch (err) { setError(err.message); }
    finally { setSaving(false); }
  };

  return <section className="page-section"><div className="container">
    <div className="page-heading"><span className="eyebrow">Diagnostics</span><h1>Laboratory tests</h1><p>Browse available tests, review preparation notes, and choose an open collection time.</p></div>
    <label className="search-field"><Search size={18} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search tests or categories" /></label>
    {loading && <p className="notice">Loading test catalogue…</p>}{error && !selected && <p className="notice notice-error" role="alert">{error}</p>}
    {tests.some((test) => test.code?.startsWith('DEMO-')) && <div className="demo-data-banner"><strong>Demo laboratory catalogue</strong><span>These fictional sample tests and prices are for demonstrating the app only. They are not real laboratory services or medical advice.</span></div>}
    {!loading && !error && tests.length === 0 && <div className="empty-state"><TestTube2 size={30} /><h2>No tests are listed yet</h2><p>A laboratory administrator can add tests to publish the catalogue.</p></div>}
    <div className="catalogue-grid">{filtered.map((test) => { const demo = test.code?.startsWith('DEMO-'); return <article className="catalogue-card lab-card" key={test.id}><div className="catalogue-icon"><TestTube2 size={20} /></div><div className="catalogue-title"><span>{test.category}</span><h2>{test.name}</h2></div>{demo && <span className="lab-demo-tag">Demo sample</span>}<p className="lab-description">{test.description || 'Diagnostic laboratory test'}</p><div className="lab-meta"><span>{test.fastingRequired ? 'Fasting required' : 'No fasting required'}</span><strong>₹{Number(test.price).toFixed(2)}</strong></div><button className="btn btn-primary" onClick={() => { setSelected(test); setSuccess(null); }}>{demo ? 'View demo test & times' : 'View details & book'}</button></article>; })}</div>
    {selected && <div className="modal-overlay" onMouseDown={(event) => event.target === event.currentTarget && setSelected(null)}><section className="modal-content doctor-modal" role="dialog" aria-modal="true"><header className="auth-header"><div><span className="eyebrow">{selected.category}</span><h2>{selected.name}</h2></div><button className="icon-button" onClick={() => setSelected(null)} aria-label="Close test details"><X size={20} /></button></header>
      {success ? <div className="booking-success"><TestTube2 size={32} /><h3>Test booked</h3><p>{success.testName} is booked for {success.date} at {success.time}. The invoice is available in Billing.</p><button className="btn btn-primary" onClick={() => setSelected(null)}>Done</button></div> : <div className="doctor-detail"><p>{selected.description}</p><div className="doctor-detail-facts"><span>{selected.fastingRequired ? 'Fasting required' : 'No fasting required'}</span><strong>₹{selected.price} <small>per test</small></strong></div><label className="form-label"><CalendarDays size={16} /> Choose a date<input type="date" min={tomorrow()} value={date} onChange={(e) => setDate(e.target.value)} /></label><h3><Clock3 size={17} /> Available times</h3>{times.length ? <div className="slot-grid">{times.map((slot) => <button key={slot} className={time === slot ? 'slot-button selected' : 'slot-button'} onClick={() => setTime(slot)}>{slot.slice(0, 5)}</button>)}</div> : <p className="muted small">No collection times available for this date.</p>}<label className="form-label">Collection method<select value={method} onChange={(e) => setMethod(e.target.value)}><option value="IN_PERSON">Visit the laboratory</option><option value="HOME_COLLECTION">Home collection</option></select></label>{error && <p className="notice notice-error" role="alert">{error}</p>}<button className="btn btn-primary full-width" disabled={!time || saving} onClick={book}>{!isAuthenticated ? 'Sign in to book this test' : saving ? 'Booking…' : 'Confirm test booking'}</button></div>}
    </section></div>}
  </div></section>;
}
