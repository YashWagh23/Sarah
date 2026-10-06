import React, { useEffect, useRef, useState } from 'react';
import { BatteryCharging, ChevronDown, Moon, Target, WifiOff, Zap } from 'lucide-react';
import { BottomNav, type TabId } from './BottomNav';
import { useUserProfile } from '../context/UserProfileContext';
import { type EnergyLevel } from '../lib/db';
import { ENERGY_PROFILES } from '../lib/planner';

const ENERGY_OPTIONS: Array<{ id: EnergyLevel; icon: React.ComponentType<{ size?: number }> }> = [
  { id: 'high', icon: Zap },
  { id: 'normal', icon: Target },
  { id: 'low', icon: BatteryCharging },
  { id: 'exhausted', icon: Moon }
];

interface AppShellProps {
  activeTab: TabId;
  onTabSelect: (tab: TabId) => void;
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({ activeTab, onTabSelect, children }) => {
  const [isOnline, setIsOnline] = useState<boolean>(() => (typeof navigator !== 'undefined' ? navigator.onLine : true));
  const { profile, setEnergyLevel } = useUserProfile();
  const [isEnergyMenuOpen, setIsEnergyMenuOpen] = useState(false);
  const energyMenuRef = useRef<HTMLDivElement>(null);
  const mainRef = useRef<HTMLElement>(null);

  const active = ENERGY_OPTIONS.find(o => o.id === profile.energyLevel) ?? ENERGY_OPTIONS[1];
  const ActiveIcon = active.icon;

  useEffect(() => {
    if (!isEnergyMenuOpen) return;
    const onPointer = (event: MouseEvent | TouchEvent) => {
      if (!energyMenuRef.current?.contains(event.target as Node)) setIsEnergyMenuOpen(false);
    };
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsEnergyMenuOpen(false);
    };
    document.addEventListener('mousedown', onPointer);
    document.addEventListener('touchstart', onPointer);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onPointer);
      document.removeEventListener('touchstart', onPointer);
      document.removeEventListener('keydown', onKey);
    };
  }, [isEnergyMenuOpen]);

  useEffect(() => {
    const on = () => setIsOnline(true);
    const off = () => setIsOnline(false);
    window.addEventListener('online', on);
    window.addEventListener('offline', off);
    return () => {
      window.removeEventListener('online', on);
      window.removeEventListener('offline', off);
    };
  }, []);

  // Each tab starts at the top instead of inheriting the last tab's scroll.
  useEffect(() => {
    mainRef.current?.scrollTo({ top: 0 });
  }, [activeTab]);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100dvh',
        width: '100%',
        maxWidth: 560,
        margin: '0 auto',
        background: 'var(--bg)',
        position: 'relative'
      }}
    >
      <header
        className="safe-top"
        style={{
          background: 'var(--chrome-bg)',
          backdropFilter: 'blur(18px) saturate(160%)',
          WebkitBackdropFilter: 'blur(18px) saturate(160%)',
          borderBottom: '1px solid var(--line)',
          zIndex: 40,
          flexShrink: 0
        }}
      >
        <div className="row" style={{ justifyContent: 'space-between', height: 56, padding: '0 12px 0 16px' }}>
          <div className="row-8">
            <img src="./sarah_logo.png" alt="" width={28} height={28} style={{ borderRadius: 8, display: 'block' }} />
            <span style={{ fontSize: 17, fontWeight: 700, letterSpacing: '-0.02em' }}>Sarah</span>
            {!isOnline && (
              <span className="tag" title="Offline. Everything still works and saves on this device.">
                <WifiOff size={12} />
                Offline
              </span>
            )}
          </div>

          <div ref={energyMenuRef} style={{ position: 'relative' }}>
            <button
              type="button"
              aria-haspopup="menu"
              aria-expanded={isEnergyMenuOpen}
              aria-label={`Energy: ${ENERGY_PROFILES[active.id].label}. Change`}
              onClick={() => setIsEnergyMenuOpen(o => !o)}
              className="chip press"
              style={{ height: 34 }}
            >
              <ActiveIcon size={15} />
              <span style={{ fontWeight: 600, color: 'var(--text)' }}>{ENERGY_PROFILES[active.id].label}</span>
              <ChevronDown size={14} />
            </button>

            {isEnergyMenuOpen && (
              <div
                role="menu"
                aria-label="Energy level"
                className="list"
                style={{
                  position: 'absolute',
                  right: 0,
                  top: 42,
                  width: 236,
                  zIndex: 60,
                  boxShadow: 'var(--shadow-2)',
                  animation: 'pop 0.16s var(--ease-out) both'
                }}
              >
                {ENERGY_OPTIONS.map(opt => {
                  const Icon = opt.icon;
                  const isSelected = profile.energyLevel === opt.id;
                  return (
                    <button
                      key={opt.id}
                      type="button"
                      role="menuitemradio"
                      aria-checked={isSelected}
                      className="list-row"
                      style={{ minHeight: 52, color: isSelected ? 'var(--accent-text)' : 'var(--text)' }}
                      onClick={() => {
                        setEnergyLevel(opt.id);
                        setIsEnergyMenuOpen(false);
                      }}
                    >
                      <Icon size={17} />
                      <span className="grow">
                        <span style={{ display: 'block', fontSize: 14, fontWeight: isSelected ? 650 : 550 }}>
                          {ENERGY_PROFILES[opt.id].label}
                        </span>
                        <span className="meta">{ENERGY_PROFILES[opt.id].hint}</span>
                      </span>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </header>

      <main ref={mainRef} className="scroll-container" style={{ flex: 1, position: 'relative' }}>
        {children}
      </main>

      <BottomNav activeTab={activeTab} onTabSelect={onTabSelect} />
    </div>
  );
};
