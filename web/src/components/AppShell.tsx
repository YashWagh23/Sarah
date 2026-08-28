import React, { useState, useEffect } from 'react';
import { BottomNav, type TabId } from './BottomNav';

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

  const currentDate = new Date().toLocaleDateString('en-US', {
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
          background: 'rgba(249, 249, 251, 0.88)',
          backdropFilter: 'blur(20px) saturate(180%)',
          WebkitBackdropFilter: 'blur(20px) saturate(180%)',
          borderBottom: '1px solid rgba(226, 226, 232, 0.6)',
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
                boxShadow: '0 2px 6px rgba(68, 80, 183, 0.2)',
                backgroundColor: '#ffffff',
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

          {/* Top Right Status Badge */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              backgroundColor: isOnline ? 'rgba(68, 80, 183, 0.08)' : 'rgba(245, 158, 11, 0.1)',
              padding: '4px 10px',
              borderRadius: '20px',
              border: isOnline ? '1px solid rgba(68, 80, 183, 0.15)' : '1px solid rgba(245, 158, 11, 0.25)'
            }}
          >
            <span
              style={{
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                backgroundColor: isOnline ? '#10B981' : '#F59E0B'
              }}
            />
            <span
              style={{
                fontSize: '11px',
                fontWeight: 600,
                color: isOnline ? 'var(--sarah-primary)' : 'var(--sarah-tertiary)'
              }}
            >
              {isOnline ? 'Online' : 'Offline Ready'}
            </span>
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
