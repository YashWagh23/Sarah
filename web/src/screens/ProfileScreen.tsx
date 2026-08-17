import React, { useState, useEffect } from 'react';
import { 
  Smartphone, 
  Database, 
  Share2, 
  PlusSquare, 
  CheckCircle2 
} from 'lucide-react';
import { initializeAndTrackPersistence, type PersistenceStatus } from '../lib/db';

export const ProfileScreen: React.FC = () => {
  const [persistence, setPersistence] = useState<PersistenceStatus | null>(null);

  useEffect(() => {
    initializeAndTrackPersistence().then(setPersistence);
  }, []);

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 80px 18px',
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
          Sarah PWA configuration and device status
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
          background: 'rgba(255, 255, 255, 0.9)'
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
            fontWeight: 700
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

      {/* iPhone Safari Install Guide Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Smartphone size={18} color="var(--sarah-primary)" />
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            iPhone Safari Home Screen Install
          </span>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          To use Sarah like a native iPhone app without browser address bars:
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12px', color: 'var(--sarah-on-background)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              1
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Tap Safari's <Share2 size={13} color="var(--sarah-primary)" /> <strong>Share</strong> button
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              2
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Select <PlusSquare size={13} color="var(--sarah-primary)" /> <strong>Add to Home Screen</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              3
            </div>
            <div>
              Launch Sarah from Home Screen in standalone mode
            </div>
          </div>
        </div>
      </div>

      {/* IndexedDB Diagnostic Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Database size={16} color="var(--sarah-primary)" />
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Storage Diagnostic
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#059669', fontWeight: 600 }}>
            <CheckCircle2 size={13} />
            <span>IndexedDB Ready</span>
          </div>
        </div>

        <div style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Database:</span>
            <span style={{ fontWeight: 600, color: 'var(--sarah-on-background)' }}>sarah_pwa_db (v1)</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Object Store:</span>
            <span style={{ fontWeight: 600, color: 'var(--sarah-on-background)' }}>key_val</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Session Reload Count:</span>
            <span style={{ fontWeight: 600, color: 'var(--sarah-primary)' }}>{persistence?.reloadCount ?? 1}</span>
          </div>
        </div>
      </div>

      {/* Sarah Info & Milestone Badge */}
      <div style={{ textAlign: 'center', padding: '10px 0', color: 'var(--sarah-secondary)', fontSize: '11px' }}>
        <div style={{ fontWeight: 600, color: 'var(--sarah-primary)' }}>
          Sarah PWA • Milestone 1 Foundation
        </div>
        <div style={{ marginTop: '2px' }}>
          Separate Web Client • Native KMP Core Untouched
        </div>
      </div>
    </div>
  );
};
