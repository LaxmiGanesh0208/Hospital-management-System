import React from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  HeartPulse, 
  User, 
  LogOut, 
  Stethoscope, 
  Pill, 
  TestTube2, 
  Receipt,
  LayoutDashboard 
} from 'lucide-react';

export const Navigation = ({ activeTab, setActiveTab, openAuthModal }) => {
  const { user, isAuthenticated, isAdmin, isStaff, logout } = useAuth();

  return (
    <header style={{
      position: 'sticky',
      top: 0,
      zIndex: 900,
      background: '#ffffff',
      borderBottom: '1px solid #e2e8f0',
      boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '76px'
      }}>
        {/* Brand Logo */}
        <div 
          onClick={() => setActiveTab('home')}
          style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
        >
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #0d9488 0%, #0284c7 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(13, 148, 136, 0.25)'
          }}>
            <HeartPulse size={26} color="#ffffff" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#0f172a', letterSpacing: '-0.5px', lineHeight: 1.1 }}>
              MEDICARE<span style={{ color: '#0d9488' }}>+</span>
            </h2>
            <span style={{ fontSize: '0.68rem', color: '#64748b', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: 700 }}>
              Hospital & Medical Center
            </span>
          </div>
        </div>

        {/* Center Nav Links */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          {[
            { id: 'home', label: 'Home' },
            { id: 'doctors', label: 'Doctors', icon: Stethoscope },
            { id: 'pharmacy', label: 'Pharmacy', icon: Pill },
            { id: 'laboratory', label: 'Laboratory', icon: TestTube2 },
            { id: 'billing', label: 'Billing', icon: Receipt },
          ].map(item => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  background: isActive ? '#f0fdf4' : 'transparent',
                  color: isActive ? '#0d9488' : '#475569',
                  border: isActive ? '1px solid #99f6e4' : '1px solid transparent',
                  padding: '8px 16px',
                  borderRadius: '10px',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                {Icon && <Icon size={16} color={isActive ? '#0d9488' : '#64748b'} />}
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* Right Action / Auth Buttons */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <button
                onClick={() => setActiveTab(isAdmin ? 'admin-dashboard' : isStaff ? 'department-dashboard' : 'patient-dashboard')}
                className="btn btn-primary"
                style={{ padding: '8px 16px', fontSize: '0.85rem' }}
              >
                <LayoutDashboard size={16} />
                {isAdmin ? 'Administration' : isStaff ? 'Department' : 'My account'}
              </button>

              <div style={{
                background: '#f8fafc',
                padding: '6px 12px',
                borderRadius: '10px',
                border: '1px solid #e2e8f0',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <div style={{
                  width: '28px',
                  height: '28px',
                  borderRadius: '50%',
                  background: '#0d9488',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 'bold',
                  fontSize: '0.8rem',
                  color: '#fff'
                }}>
                  {user?.name ? user.name.charAt(0) : 'U'}
                </div>
                <div style={{ lineHeight: 1.2 }}>
                  <p style={{ fontSize: '0.8rem', fontWeight: 700, color: '#0f172a' }}>{user?.name || 'User'}</p>
                <p style={{ fontSize: '0.65rem', color: '#64748b' }}>{user?.role || 'Signed in'}</p>
                </div>
              </div>

              <button
                onClick={logout}
                title="Logout"
                style={{
                  background: '#fef2f2',
                  color: '#ef4444',
                  border: '1px solid #fecaca',
                  padding: '8px',
                  borderRadius: '10px',
                  cursor: 'pointer'
                }}
              >
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <button
                onClick={() => openAuthModal('PATIENT')}
                className="btn btn-primary"
              >
                <User size={16} />
                Sign in
              </button>

            </div>
          )}
        </div>
      </div>
    </header>
  );
};
