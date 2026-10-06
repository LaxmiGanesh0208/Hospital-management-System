import React, { useState } from 'react';
import './App.css';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navigation } from './components/Navigation';
import { AuthModal } from './components/AuthModal';
import { DoctorDirectory } from './components/DoctorDirectory';
import { PharmacySection } from './components/PharmacySection';
import { LaboratorySection } from './components/LaboratorySection';
import { BillingSection } from './components/BillingSection';
import { PatientDashboard } from './components/PatientDashboard';
import { AdminDashboard } from './components/AdminDashboard';
import { DepartmentDashboard } from './components/DepartmentDashboard';
import { HomepageChatbot } from './components/HomepageChatbot';
import { Activity, ArrowRight, HeartPulse, LockKeyhole, Pill, Receipt, Stethoscope, TestTube2 } from 'lucide-react';

function ServiceCard({ icon: Icon, title, description, status, onClick }) {
  return <button className="service-card" onClick={onClick}><span className="service-icon">{React.createElement(Icon, { size: 21 })}</span><span className="service-copy"><strong>{title}</strong><span>{description}</span><small>{status}</small></span><ArrowRight size={18} className="service-arrow" /></button>;
}

function Home({ setActiveTab, openAuthModal }) {
  const { isAuthenticated } = useAuth();
  return <>
    <section className="hero"><div className="container hero-inner"><div className="hero-copy"><span className="eyebrow"><HeartPulse size={15} /> Connected care</span><h1>Healthcare, made<br /><em>easier to navigate.</em></h1><p>Find a doctor, book an appointment, or browse pharmacy and laboratory services.</p><div className="hero-actions"><button className="btn btn-primary" onClick={() => setActiveTab('doctors')}>Explore services <ArrowRight size={17} /></button>{!isAuthenticated && <button className="btn btn-secondary" onClick={openAuthModal}>Sign in</button>}</div><div className="hero-note"><Activity size={16} /> {isAuthenticated ? 'Your care account is ready' : 'Appointments and care services in one place'}</div></div><div className="hero-art"><div className="hero-circle"><HeartPulse size={86} strokeWidth={1.25} /></div><div className="hero-card hero-card-top"><Stethoscope size={16} /> Doctor appointments</div><div className="hero-card hero-card-bottom"><Pill size={16} /> Pharmacy & prescriptions</div></div></div></section>
    <section className="services-section"><div className="container"><div className="section-heading"><div><span className="eyebrow">Services</span><h2>What do you need today?</h2></div><p>Choose a service to view information and available appointments.</p></div><div className="service-grid">
      <ServiceCard icon={Stethoscope} title="Doctors" description="Meet our doctors and book an appointment" status="View available times" onClick={() => setActiveTab('doctors')} />
      <ServiceCard icon={Pill} title="Pharmacy" description="Browse medicines and prescription requirements" status={isAuthenticated ? 'Browse medicines' : 'Sign in to place an order'} onClick={() => setActiveTab('pharmacy')} />
      <ServiceCard icon={TestTube2} title="Laboratory" description="Explore tests, bookings, and results" status="View laboratory services" onClick={() => setActiveTab('laboratory')} />
      <ServiceCard icon={Receipt} title="Billing" description="Review invoices for your care" status={isAuthenticated ? 'View your invoices' : 'Sign in to view invoices'} onClick={() => setActiveTab('billing')} />
    </div></div></section>
    <HomepageChatbot onNavigate={setActiveTab} />
  </>;
}

function AppContent() {
  const [activeTab, setActiveTab] = useState('home');
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [authTab, setAuthTab] = useState('PATIENT');
  const { isAuthenticated, isAdmin, isStaff } = useAuth();
  const openAuthModal = (tab = 'PATIENT') => { setAuthTab(tab); setAuthModalOpen(true); };
  return <div className="app-shell"><div className="public-demo-notice"><strong>Public demo</strong><span>Use fictional details only. Do not enter real personal or medical information. Not for clinical or emergency use.</span></div><Navigation activeTab={activeTab} setActiveTab={setActiveTab} openAuthModal={openAuthModal} /><main>
    {activeTab === 'home' && <Home setActiveTab={setActiveTab} openAuthModal={openAuthModal} />}
    {activeTab === 'doctors' && <DoctorDirectory openAuthModal={openAuthModal} />}
    {activeTab === 'pharmacy' && <PharmacySection openAuthModal={openAuthModal} />}
    {activeTab === 'laboratory' && <LaboratorySection openAuthModal={openAuthModal} />}
    {activeTab === 'billing' && <BillingSection openAuthModal={openAuthModal} />}
    {activeTab === 'patient-dashboard' && (isAuthenticated && !isAdmin ? <PatientDashboard setActiveTab={setActiveTab} /> : <div className="container page-section"><div className="empty-state"><LockKeyhole /><h2>Patient sign in required</h2><button className="btn btn-primary" onClick={() => openAuthModal('PATIENT')}>Sign in</button></div></div>)}
    {activeTab === 'admin-dashboard' && (isAuthenticated && isAdmin ? <AdminDashboard /> : <div className="container page-section"><div className="empty-state"><LockKeyhole /><h2>Administration sign in required</h2><button className="btn btn-primary" onClick={() => openAuthModal('ADMIN')}>Administration sign in</button></div></div>)}
    {activeTab === 'department-dashboard' && (isAuthenticated && isStaff ? <DepartmentDashboard /> : <div className="container page-section"><div className="empty-state"><LockKeyhole /><h2>Department sign in required</h2><button className="btn btn-primary" onClick={() => openAuthModal('DEPARTMENT')}>Department sign in</button></div></div>)}
  </main><footer className="site-footer"><div className="container"><span><HeartPulse size={18} /> Medicare+</span><small>Your care, in one place</small></div></footer><AuthModal isOpen={authModalOpen} initialTab={authTab} onAuthenticated={(user) => setActiveTab(['ADMIN', 'SUPER_ADMIN'].includes(user.role) ? 'admin-dashboard' : ['PHARMACIST', 'PHARMACY_REVIEWER', 'LAB_TECH', 'LAB_REVIEWER', 'DOCTOR', 'DOCTOR_REVIEWER'].includes(user.role) ? 'department-dashboard' : 'patient-dashboard')} onClose={() => setAuthModalOpen(false)} /></div>;
}

export default function App() { return <AuthProvider><AppContent /></AuthProvider>; }
