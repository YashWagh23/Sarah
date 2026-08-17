import React, { useState, useEffect } from 'react';
import { 
  Smartphone, 
  Share2, 
  PlusSquare, 
  CheckCircle2, 
  Bell, 
  BellRing, 
  AlertTriangle, 
  ShieldCheck, 
  Sparkles 
} from 'lucide-react';
import { initializeAndTrackPersistence, type PersistenceStatus } from '../lib/db';
import { useReminders } from '../context/RemindersContext';

export const ProfileScreen: React.FC = () => {
  const [persistence, setPersistence] = useState<PersistenceStatus | null>(null);
  const { notificationPermission, requestNotificationPermission } = useReminders();

  useEffect(() => {
    initializeAndTrackPersistence().then(setPersistence);
  }, []);

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '18px'
      }}
    >
      <div>
        <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0 }}>
          Profile & Settings
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
          Personal college assistant preferences
        </p>
      </div>

      {/* User Card */}
      <div
        className="glass-card"
        style={{
          padding: '18px',
          display: 'flex',
          alignItems: 'center',
          gap: '14px',
          background: 'rgba(255, 255, 255, 0.92)'
        }}
      >
        <div
          style={{
            width: '52px',
            height: '52px',
            borderRadius: '16px',
            backgroundColor: 'var(--sarah-primary-fixed)',
            color: 'var(--sarah-primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '20px',
            fontWeight: 800
          }}
        >
          Y
        </div>
        <div>
          <div style={{ fontSize: '17px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            Yash Wagh
          </div>
          <div style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)' }}>
            Computer Science & Engineering
          </div>
          <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
            Semester 6 • College Schedule Active
          </div>
        </div>
      </div>

      {/* Local Storage & Privacy Status */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShieldCheck size={18} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Local Data & Privacy
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#059669', fontWeight: 600 }}>
            <CheckCircle2 size={13} />
            <span>Secure & Offline</span>
          </div>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.5, margin: 0 }}>
          Your tasks, notes, reminders, and courses are stored safely on this device. No cloud sync or external tracking required.
        </p>

        <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)', backgroundColor: 'var(--sarah-surface-container-low)', padding: '6px 10px', borderRadius: '8px', display: 'flex', justifyContent: 'space-between' }}>
          <span>Device Sessions: {persistence?.reloadCount ?? 1}</span>
          <span>Offline Ready ✓</span>
        </div>
      </div>

      {/* Browser Notifications & Reminders Status Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Bell size={17} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Notifications & Alerts
            </span>
          </div>

          {/* Status Chip */}
          {notificationPermission === 'granted' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#059669', fontWeight: 600 }}>
              <CheckCircle2 size={13} />
              <span>Enabled</span>
            </div>
          )}
          {notificationPermission === 'denied' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-error)', fontWeight: 600 }}>
              <AlertTriangle size={13} />
              <span>Blocked</span>
            </div>
          )}
          {notificationPermission === 'default' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              <span>Not Requested</span>
            </div>
          )}
          {notificationPermission === 'unsupported' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              <span>In-App Alerts Only</span>
            </div>
          )}
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          Sarah delivers quiet reminders and alerts when deadlines and study sessions arrive while the app is active.
        </p>

        {notificationPermission === 'default' && (
          <button
            type="button"
            onClick={() => requestNotificationPermission()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              backgroundColor: 'rgba(68, 80, 183, 0.1)',
              color: 'var(--sarah-primary)',
              border: '1px solid rgba(68, 80, 183, 0.2)',
              borderRadius: '12px',
              padding: '10px 14px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <BellRing size={15} />
            <span>Enable Notifications</span>
          </button>
        )}

        {notificationPermission === 'granted' && (
          <div style={{ fontSize: '11.5px', color: 'var(--sarah-secondary)', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 12px', borderRadius: '10px' }}>
            ✓ Browser notifications are authorized and active.
          </div>
        )}
      </div>

      {/* iPhone Safari Install Guide Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Smartphone size={18} color="var(--sarah-primary)" />
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            Install on iPhone
          </span>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          For the full standalone Apple app experience without Safari browser bars:
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '9px', fontSize: '12.5px', color: 'var(--sarah-on-background)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              1
            </div>
            <div>
              Open Sarah in <strong>Safari</strong> on your iPhone
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              2
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Tap Safari's <Share2 size={13} color="var(--sarah-primary)" /> <strong>Share</strong> button
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              3
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Select <PlusSquare size={13} color="var(--sarah-primary)" /> <strong>Add to Home Screen</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              4
            </div>
            <div>
              Tap <strong>Add</strong> in the top-right corner
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-primary)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              5
            </div>
            <div>
              Launch <strong>Sarah</strong> directly from your Home Screen
            </div>
          </div>
        </div>
      </div>

      {/* Sarah Info & Branding */}
      <div style={{ textAlign: 'center', padding: '10px 0', color: 'var(--sarah-secondary)', fontSize: '11px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', fontWeight: 700, color: 'var(--sarah-primary)' }}>
          <Sparkles size={13} />
          <span>Sarah • Personal College Assistant</span>
        </div>
        <div style={{ marginTop: '2px' }}>
          Apple-Inspired Progressive Web App • Offline Ready
        </div>
      </div>
    </div>
  );
};
