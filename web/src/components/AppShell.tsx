import React, { useEffect, useRef, useState } from 'react';
import { BatteryCharging, Moon, Target, Zap } from 'lucide-react';
import { BottomNav, type TabId } from './BottomNav';
import { useUserProfile } from '../context/UserProfileContext';
import { type EnergyLevel } from '../lib/db';
import { useNow } from '../lib/datetime';

const ENERGY_OPTIONS: Array<{ id: EnergyLevel; label: string; icon: React.ComponentType<{ size?: number }> }> = [
  { id: 'high', label: 'High energy', icon: Zap },
  { id: 'normal', label: 'Steady', icon: Target },
  { id: 'low', label: 'Low energy', icon: BatteryCharging },
  { id: 'exhausted', label: 'Resting', icon: Moon }
];

interface AppShellProps {
  activeTab: TabId;
  onTabSelect: (tab: TabId) => void;
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({
  activeTab,
  onTabSelect,
  children
}) => {
  const [isOnline, setIsOnline] = useState<boolean>(() => {
    return typeof navigator !== 'undefined' ? navigator.onLine : true;
  });
  const { profile, setEnergyLevel } = useUserProfile();
  const [isEnergyMenuOpen, setIsEnergyMenuOpen] = useState(false);
  const energyMenuRef = useRef<HTMLDivElement>(null);

  const activeEnergy = ENERGY_OPTIONS.find(o => o.id === profile.energyLevel) ?? ENERGY_OPTIONS[1];
  const ActiveEnergyIcon = activeEnergy.icon;

  useEffect(() => {
    if (!isEnergyMenuOpen) return;
    const handlePointerDown = (event: MouseEvent | TouchEvent) => {
      if (!energyMenuRef.current?.contains(event.target as Node)) {
        setIsEnergyMenuOpen(false);
      }
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsEnergyMenuOpen(false);
    };
    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('touchstart', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('touchstart', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isEnergyMenuOpen]);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Ticks so a PWA left open overnight shows the new day, not the launch day.
  const now = useNow(60000);
  const currentDate = new Date(now).toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric'
  });

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100dvh',
        width: '100%',
        maxWidth: '540px',
        margin: '0 auto',
        backgroundColor: 'var(--sarah-background)',
        position: 'relative',
        boxShadow: '0 0 50px rgba(0, 0, 0, 0.05)',
      }}
    >
      {/* Top Header Bar with Safe Area Top */}
      <header
        className="safe-top"
        style={{
          width: '100%',
          background: 'var(--sarah-header-bg)',
          backdropFilter: 'blur(20px) saturate(180%)',
          WebkitBackdropFilter: 'blur(20px) saturate(180%)',
          borderBottom: '1px solid var(--sarah-header-border)',
          zIndex: 40,
          flexShrink: 0
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '10px 18px',
            height: '52px'
          }}
        >
          {/* Brand Logo & Name */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px'
            }}
          >
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '9px',
                overflow: 'hidden',
                boxShadow: '0 2px 6px rgba(var(--sarah-primary-rgb), 0.2)',
                backgroundColor: 'var(--sarah-surface-card)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <img
                src="./sarah_logo.png"
                alt="Sarah Logo"
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover'
                }}
                onError={(e) => {
                  // Fallback to favicon or icon if needed
                  (e.target as HTMLImageElement).src = './favicon.png';
                }}
              />
            </div>
            <div>
              <h1
                style={{
                  fontSize: '17px',
                  fontWeight: 700,
                  color: 'var(--sarah-on-background)',
                  letterSpacing: '-0.02em',
                  lineHeight: 1.1
                }}
              >
                Sarah
              </h1>
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 500,
                  color: 'var(--sarah-secondary)'
                }}
              >
                {currentDate}
              </span>
            </div>
          </div>

          {/* Top Right: connectivity dot + energy switcher */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span
              title={isOnline ? 'Online' : 'Offline — everything still works'}
              aria-label={isOnline ? 'Online' : 'Offline ready'}
              style={{
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                backgroundColor: isOnline ? 'var(--sarah-success)' : 'var(--sarah-amber)',
                flexShrink: 0
              }}
            />

            <div ref={energyMenuRef} style={{ position: 'relative' }}>
              <button
                type="button"
                aria-haspopup="menu"
                aria-expanded={isEnergyMenuOpen}
                aria-label={`Energy level: ${activeEnergy.label}. Change it.`}
                onClick={() => setIsEnergyMenuOpen(open => !open)}
                className="btn-press"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  backgroundColor: 'rgba(var(--sarah-primary-rgb), 0.1)',
                  border: '1px solid rgba(var(--sarah-primary-rgb), 0.18)',
                  padding: '5px 11px',
                  borderRadius: '20px',
                  cursor: 'pointer',
                  color: 'var(--sarah-primary)',
                  fontSize: '11.5px',
                  fontWeight: 600
                }}
              >
                <ActiveEnergyIcon size={13} />
                <span>{activeEnergy.label}</span>
              </button>

              {isEnergyMenuOpen && (
                <div
                  role="menu"
                  aria-label="Energy level"
                  className="glass-card"
                  style={{
                    position: 'absolute',
                    right: 0,
                    top: '38px',
                    width: '170px',
                    padding: '5px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '2px',
                    backgroundColor: 'var(--sarah-elevated)',
                    boxShadow: 'var(--menu-shadow)',
                    borderRadius: '14px',
                    zIndex: 60
                  }}
                >
                  {ENERGY_OPTIONS.map((opt) => {
                    const Icon = opt.icon;
                    const isSelected = profile.energyLevel === opt.id;
                    return (
                      <button
                        key={opt.id}
                        type="button"
                        role="menuitemradio"
                        aria-checked={isSelected}
                        onClick={() => {
                          setEnergyLevel(opt.id);
                          setIsEnergyMenuOpen(false);
                        }}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          padding: '8px 10px',
                          border: 'none',
                          borderRadius: '10px',
                          cursor: 'pointer',
                          textAlign: 'left',
                          fontSize: '12.5px',
                          fontWeight: isSelected ? 700 : 500,
                          backgroundColor: isSelected ? 'rgba(var(--sarah-primary-rgb), 0.1)' : 'transparent',
                          color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-on-background)'
                        }}
                      >
                        <Icon size={14} />
                        <span>{opt.label}</span>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main
        className="scroll-container"
        style={{
          flex: 1,
          width: '100%',
          position: 'relative'
        }}
      >
        {children}
      </main>

      {/* Bottom Navigation */}
      <BottomNav activeTab={activeTab} onTabSelect={onTabSelect} />
    </div>
  );
};
