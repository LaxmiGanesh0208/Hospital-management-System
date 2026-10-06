import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api } from '../api';

const AuthContext = createContext(null);

function readSavedUser() {
  try { return JSON.parse(localStorage.getItem('hospital_user') || 'null'); }
  catch { return null; }
}

function saveSession(result) {
  const user = { id: result.userId || result.id, email: result.email, role: result.role, fullName: result.fullName, doctorId: result.doctorId };
  if (!result.token) throw new Error('The authentication service did not return a token.');
  localStorage.setItem('hospital_token', result.token);
  localStorage.setItem('hospital_user', JSON.stringify(user));
  return user;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readSavedUser);
  const [token, setToken] = useState(() => localStorage.getItem('hospital_token'));
  const [isCheckingSession, setIsCheckingSession] = useState(Boolean(localStorage.getItem('hospital_token')));

  const login = async ({ identifier, password }) => {
    const result = await api.login({ identifier, password });
    const next = saveSession(result);
    setToken(result.token); setUser(next);
    return next;
  };

  const register = async (profile) => {
    const result = await api.register(profile);
    const next = saveSession(result);
    setToken(result.token); setUser(next);
    return next;
  };

  const logout = async () => {
    try { if (localStorage.getItem('hospital_token')) await api.logout(); }
    catch { /* Always clear the local session, even if the server is unreachable. */ }
    finally {
      localStorage.removeItem('hospital_token'); localStorage.removeItem('hospital_user');
      setToken(null); setUser(null);
    }
  };

  useEffect(() => {
    if (!token) { setIsCheckingSession(false); return undefined; }
    let active = true;
    api.profile().then((profile) => {
      if (!active) return;
      const safeUser = { id: profile.id, email: profile.email, role: profile.role, fullName: profile.fullName, doctorId: profile.doctorId };
      localStorage.setItem('hospital_user', JSON.stringify(safeUser)); setUser(safeUser);
    }).catch(() => {
      if (!active) return;
      localStorage.removeItem('hospital_token'); localStorage.removeItem('hospital_user');
      setToken(null); setUser(null);
    }).finally(() => { if (active) setIsCheckingSession(false); });
    return () => { active = false; };
  }, []);

  const role = user?.role || '';
  const value = useMemo(() => ({
    user, token, role, isAuthenticated: Boolean(token),
    isPatient: role === 'PATIENT', isAdmin: ['ADMIN', 'SUPER_ADMIN'].includes(role),
    isStaff: ['PHARMACIST', 'PHARMACY_REVIEWER', 'LAB_TECH', 'LAB_REVIEWER', 'DOCTOR', 'DOCTOR_REVIEWER'].includes(role),
    isCheckingSession, login, register, logout,
  }), [user, token, role, isCheckingSession]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
