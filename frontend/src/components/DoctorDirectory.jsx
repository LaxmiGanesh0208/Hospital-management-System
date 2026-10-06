import React, { useEffect, useMemo, useState } from 'react';
import { CalendarDays, Clock3, Search, Stethoscope, UserRound, X } from 'lucide-react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';

function getDateOffset(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function formatDate(value) {
  return new Date(`${value}T12:00:00`).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
}

function formatTime(value) {
  if (!value) return '';
  const [hours, minutes] = value.split(':').map(Number);
  return new Date(2000, 0, 1, hours, minutes).toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' });
}

const receptionistPhone = (import.meta.env.VITE_RECEPTIONIST_PHONE || '').trim();

export function DoctorDirectory({ openAuthModal }) {
  const [doctors, setDoctors] = useState([]);
  const [upcomingSlots, setUpcomingSlots] = useState({});
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState('');
  const [specialization, setSpecialization] = useState('');
  const [availability, setAvailability] = useState([]);
  const [date, setDate] = useState(getDateOffset(0));
  const [slotId, setSlotId] = useState('');
  const [loading, setLoading] = useState(true);
  const [availabilityLoading, setAvailabilityLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [booking, setBooking] = useState(null);
  const { isAuthenticated } = useAuth();
  const today = getDateOffset(0);
  const tomorrow = getDateOffset(1);

  useEffect(() => {
    let active = true;
    setLoading(true); setError('');
    api.doctors({ q: query, specialization }).then((result) => { if (active) setDoctors(result); })
      .catch((err) => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [query, specialization]);

  useEffect(() => {
    let active = true;
    api.upcomingDoctorAvailability(7).then((slots) => {
      if (!active) return;
      const grouped = {};
      slots.forEach((slot) => { (grouped[slot.doctorId] ||= []).push(slot); });
      setUpcomingSlots(grouped);
    }).catch(() => { if (active) setUpcomingSlots({}); });
    return () => { active = false; };
  }, [doctors]);

  useEffect(() => {
    if (!selected || booking) return undefined;
    let active = true;
    setAvailability([]); setSlotId(''); setError(''); setAvailabilityLoading(true);
    api.doctorAvailability(selected.id, date).then((result) => { if (active) setAvailability(result); })
      .catch((err) => { if (active) setError(err.message); })
      .finally(() => { if (active) setAvailabilityLoading(false); });
    return () => { active = false; };
  }, [selected, date, booking]);

  const specializations = useMemo(() => [...new Set(doctors.map((item) => item.specialization).filter(Boolean))], [doctors]);

  const chooseDate = (value) => { setDate(value); setNotice(''); };

  const book = async () => {
    if (!isAuthenticated) { openAuthModal('PATIENT'); return; }
    if (!slotId) return;
    setSaving(true); setError(''); setNotice('');
    try {
      const appointment = await api.bookAppointment({ slotId });
      setBooking(appointment);
      setAvailability((items) => items.filter((item) => item.id !== slotId));
      setSlotId('');
    } catch (err) {
      if (/time has passed/i.test(err.message)) {
        setNotice('That time has passed. Showing tomorrow’s appointment times.');
        setDate(tomorrow);
      } else {
        setError(err.message);
        if (/no longer available/i.test(err.message)) {
          api.doctorAvailability(selected.id, date).then(setAvailability).catch(() => {});
        }
      }
    } finally { setSaving(false); }
  };

  const showDoctor = (doctor) => {
    setSelected(doctor); setBooking(null); setDate(getDateOffset(0)); setNotice(''); setError('');
  };

  return <section className="page-section"><div className="container">
    <div className="page-heading"><span className="eyebrow">Care team</span><h1>Find a doctor</h1><p>View doctor profiles and choose from currently available appointment times.</p></div>
    <div className="filter-row"><label className="search-field"><Search size={18} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Doctor name or specialty" /></label><select className="filter-select" value={specialization} onChange={(e) => setSpecialization(e.target.value)}><option value="">All specializations</option>{specializations.map((item) => <option key={item}>{item}</option>)}</select></div>
    {loading && <p className="notice">Loading doctors…</p>}{error && !selected && <p className="notice notice-error" role="alert">{error}</p>}
    {!loading && !error && doctors.length === 0 && <div className="empty-state"><Stethoscope size={30} /><h2>No doctors found</h2><p>The directory is empty or no doctors match these filters. Please contact the hospital for help.</p></div>}
    <div className="doctor-grid">{doctors.map((doctor) => {
      const slots = upcomingSlots[doctor.id] || [];
      const firstSlot = slots[0];
      const firstSlotDay = firstSlot ? slots.filter((slot) => slot.date === firstSlot.date).slice(0, 3) : [];
      return <article className="doctor-card" key={doctor.id}>
        <div className="doctor-card-top"><div className="doctor-avatar">{doctor.avatarUrl ? <img src={doctor.avatarUrl} alt="" /> : <UserRound size={28} />}</div><span className="doctor-specialty">{doctor.specialization}</span></div>
        <div className="doctor-main"><h2>{doctor.name}</h2><p className="doctor-qualification">{doctor.qualification || 'Consultant'}</p><p className="doctor-bio-preview">{doctor.bio || 'Experienced member of our care team, available for consultation.'}</p></div>
        <div className="doctor-facts"><span><strong>{doctor.yearsExperience ?? '—'}</strong> yrs experience</span><span><strong>₹{doctor.consultationFee}</strong> consultation</span></div>
        <div className="doctor-next-slot"><Clock3 size={16} /><div><strong>{firstSlot ? `Next available · ${formatDate(firstSlot.date)}` : 'No upcoming times listed'}</strong><span>{firstSlotDay.length ? firstSlotDay.map((slot) => `${formatTime(slot.startTime)}–${formatTime(slot.endTime)}`).join(' · ') : 'Choose a date to check availability'}</span></div></div>
        <button className="btn btn-primary" onClick={() => showDoctor(doctor)}>View profile & book</button>
      </article>;
    })}</div>
    {selected && <div className="modal-overlay" onMouseDown={(event) => event.target === event.currentTarget && setSelected(null)}><section className="modal-content doctor-modal" role="dialog" aria-modal="true" aria-labelledby="doctor-title">
      <header className="auth-header"><div><span className="eyebrow">{selected.specialization}</span><h2 id="doctor-title">{selected.name}</h2><p className="doctor-modal-qualification">{selected.qualification || 'Consultant'}</p></div><button className="icon-button" onClick={() => setSelected(null)} aria-label="Close profile"><X size={20} /></button></header>
      {booking ? <div className="booking-success"><CalendarDays size={32} /><h3>Appointment requested</h3><p>Your appointment with {booking.doctorName} is booked for {booking.date} at {formatTime(booking.startTime)}. An invoice has been added to Billing.</p><button className="btn btn-primary" onClick={() => setSelected(null)}>Done</button></div> : <div className="doctor-detail">
        <p className="doctor-full-bio">{selected.bio || 'Please contact reception for more information about this doctor.'}</p>
        <div className="doctor-detail-facts"><div><span>Experience</span><strong>{selected.yearsExperience ?? '—'} years</strong></div><div><span>Qualification</span><strong>{selected.qualification || 'Consultant'}</strong></div><div><span>Consultation</span><strong>₹{selected.consultationFee}</strong></div></div>
        <section className="booking-slots"><div className="booking-slots-heading"><div><span className="eyebrow">Appointment</span><h3><Clock3 size={17} /> Choose a time</h3></div><label className="booking-date"><CalendarDays size={16} /><input type="date" min={today} value={date} onChange={(e) => chooseDate(e.target.value)} /></label></div>
          {availabilityLoading && <p className="muted small">Checking available times…</p>}
          {!availabilityLoading && availability.length > 0 && <div className="slot-grid">{availability.map((slot) => <button type="button" key={slot.id} className={slotId === slot.id ? 'slot-button selected' : 'slot-button'} onClick={() => setSlotId(slot.id)}><strong>{formatTime(slot.startTime)}</strong><span>to {formatTime(slot.endTime)}</span></button>)}</div>}
          {!availabilityLoading && availability.length === 0 && !error && date === today && <div className="no-slots"><p>No appointment times remain today.</p><button type="button" className="btn btn-secondary" onClick={() => chooseDate(tomorrow)}>Check tomorrow’s available times · {formatDate(tomorrow)}</button></div>}
          {!availabilityLoading && availability.length === 0 && !error && date !== today && <p className="muted small">No available times on {formatDate(date)}. Please choose another date.</p>}
          {notice && <p className="notice notice-muted" role="status">{notice}</p>}{error && <p className="notice notice-error" role="alert">{error}</p>}
          <button className="btn btn-primary full-width" disabled={!slotId || saving || availabilityLoading} onClick={book}>{!isAuthenticated ? 'Sign in to book this time' : saving ? 'Booking…' : slotId ? 'Book selected time' : 'Select a time to continue'}</button>
          <p className="muted small">The consultation fee is added to your invoice after booking.</p>
        </section>
        <aside className="urgent-booking"><strong>Need urgent booking help?</strong><p>If this is a medical emergency, contact your local emergency service. For help booking an urgent appointment, contact hospital reception.</p>{receptionistPhone ? <a href={`tel:${receptionistPhone}`}>Call reception · {receptionistPhone}</a> : <span>Reception phone number has not been added yet.</span>}</aside>
      </div>}
    </section></div>}
  </div></section>;
}
