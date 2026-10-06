import React, { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const blankProfile = { fullName: '', mobile: '', email: '', dateOfBirth: '', gender: '', address: '', emergencyContact: '', password: '' };

export function AuthModal({ isOpen, onClose, onAuthenticated, initialTab = 'PATIENT' }) {
  const { login, register, logout } = useAuth();
  const [path, setPath] = useState(initialTab);
  const [isRegistering, setIsRegistering] = useState(false);
  const [profile, setProfile] = useState(blankProfile);
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => { setPath(initialTab); setIsRegistering(false); setError(''); }, [initialTab, isOpen]);
  if (!isOpen) return null;

  const changeProfile = (event) => setProfile((current) => ({ ...current, [event.target.name]: event.target.value }));
  const submit = async (event) => {
    event.preventDefault(); setBusy(true); setError('');
    try {
      let authenticatedUser;
      if (isRegistering) authenticatedUser = await register(profile);
      else {
        authenticatedUser = await login({ identifier: identifier.trim(), password });
        const staffRoles = ['PHARMACIST', 'PHARMACY_REVIEWER', 'LAB_TECH', 'LAB_REVIEWER', 'DOCTOR', 'DOCTOR_REVIEWER'];
        const validPath = path === 'ADMIN' ? ['ADMIN', 'SUPER_ADMIN'].includes(authenticatedUser.role)
          : path === 'DEPARTMENT' ? staffRoles.includes(authenticatedUser.role) : authenticatedUser.role === 'PATIENT';
        if (!validPath) {
          await logout();
          throw new Error(path === 'ADMIN' ? 'This account does not have administration access.' : path === 'DEPARTMENT' ? 'This account does not have department access.' : 'Use the correct staff sign-in for this account.');
        }
      }
      onAuthenticated?.(authenticatedUser); onClose(); setPassword('');
    } catch (err) { setError(err.message || 'Unable to complete sign in.'); }
    finally { setBusy(false); }
  };

  return <div className="modal-overlay" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <section className="modal-content auth-card" role="dialog" aria-modal="true" aria-labelledby="auth-title">
      <header className="auth-header"><div><p className="eyebrow">Secure access</p><h2 id="auth-title">{isRegistering ? 'Create your patient account' : 'Welcome back'}</h2></div><button className="icon-button" onClick={onClose} aria-label="Close sign in"><X size={20} /></button></header>
      <div className="auth-tabs"><button className={path === 'PATIENT' ? 'selected' : ''} onClick={() => { setPath('PATIENT'); setIsRegistering(false); setError(''); }}>Patient</button><button className={path === 'DEPARTMENT' ? 'selected' : ''} onClick={() => { setPath('DEPARTMENT'); setIsRegistering(false); setError(''); }}>Department</button><button className={path === 'ADMIN' ? 'selected' : ''} onClick={() => { setPath('ADMIN'); setIsRegistering(false); setError(''); }}>Administration</button></div>
      <form className="auth-form" onSubmit={submit}>
        {isRegistering ? <>
          <p className="demo-privacy-note">This is a public demo. Register with fictional details only; do not enter real contact, address, or health information.</p>
          <label>Full name<input name="fullName" autoComplete="name" required maxLength="100" value={profile.fullName} onChange={changeProfile} /></label>
          <label>Mobile number<input name="mobile" type="tel" autoComplete="tel" required value={profile.mobile} onChange={changeProfile} placeholder="+91 98765 43210" /></label>
          <label>Email address<input name="email" type="email" autoComplete="email" required value={profile.email} onChange={changeProfile} /></label>
          <label>Date of birth<input name="dateOfBirth" type="date" required value={profile.dateOfBirth} onChange={changeProfile} /></label>
          <label>Gender<select name="gender" required value={profile.gender} onChange={changeProfile}><option value="">Select</option><option>Female</option><option>Male</option><option>Non-binary</option><option>Prefer not to say</option></select></label>
          <label>Address<textarea name="address" autoComplete="street-address" required maxLength="255" rows="2" value={profile.address} onChange={changeProfile} /></label>
          <label>Emergency contact<input name="emergencyContact" type="tel" required value={profile.emergencyContact} onChange={changeProfile} /></label>
          <label>Password<input name="password" type="password" autoComplete="new-password" minLength="8" maxLength="72" required value={profile.password} onChange={changeProfile} /></label>
        </> : <>
          <label>{path === 'PATIENT' ? 'Mobile number or email' : 'Email address'}<input type="email" autoComplete="username" required value={identifier} onChange={(e) => setIdentifier(e.target.value)} /></label>
          <label>Password<input type="password" autoComplete="current-password" minLength="8" required value={password} onChange={(e) => setPassword(e.target.value)} /></label>
        </>}
        {error && <p className="notice notice-error" role="alert">{error}</p>}
        <button className="btn btn-primary" type="submit" disabled={busy}>{busy ? 'Please wait…' : isRegistering ? 'Create account' : path === 'ADMIN' ? 'Sign in to administration' : 'Sign in'}</button>
        {path === 'PATIENT' && <button className="text-button auth-switch" type="button" onClick={() => { setIsRegistering((value) => !value); setError(''); }}>{isRegistering ? 'Already registered? Sign in' : 'New patient? Create an account'}</button>}
        <p className="muted small">Sign-in uses the hospital authentication service. Accounts receive roles from the backend.</p>
      </form>
    </section>
  </div>;
}
