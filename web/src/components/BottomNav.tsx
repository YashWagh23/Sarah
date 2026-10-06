import React from 'react';
import { BookOpen, CalendarClock, ListChecks, NotebookPen, UserRound } from 'lucide-react';

export type TabId = 'today' | 'tasks' | 'notes' | 'subjects' | 'profile';

const NAV_ITEMS: Array<{ id: TabId; label: string; icon: React.ComponentType<{ size?: number }> }> = [
  { id: 'today', label: 'Today', icon: CalendarClock },
  { id: 'tasks', label: 'Tasks', icon: ListChecks },
  { id: 'notes', label: 'Notes', icon: NotebookPen },
  { id: 'subjects', label: 'Subjects', icon: BookOpen },
  { id: 'profile', label: 'Profile', icon: UserRound }
];

interface BottomNavProps {
  activeTab: TabId;
  onTabSelect: (tab: TabId) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ activeTab, onTabSelect }) => (
  <nav
    aria-label="Main"
    className="safe-bottom"
    style={{
      background: 'var(--chrome-bg)',
      backdropFilter: 'blur(18px) saturate(160%)',
      WebkitBackdropFilter: 'blur(18px) saturate(160%)',
      borderTop: '1px solid var(--line)',
      zIndex: 50,
      flexShrink: 0
    }}
  >
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', height: 60, padding: '0 4px' }}>
      {NAV_ITEMS.map(item => {
        const isSelected = activeTab === item.id;
        const Icon = item.icon;
        return (
          <button
            key={item.id}
            type="button"
            onClick={() => onTabSelect(item.id)}
            aria-current={isSelected ? 'page' : undefined}
            className="press"
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 3,
              color: isSelected ? 'var(--accent-text)' : 'var(--text-3)'
            }}
          >
            <span
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 52,
                height: 28,
                borderRadius: 'var(--r-pill)',
                background: isSelected ? 'var(--accent-soft)' : 'transparent',
                transition: 'background-color 0.2s ease'
              }}
            >
              <Icon size={20} />
            </span>
            <span style={{ fontSize: 11, fontWeight: isSelected ? 650 : 500 }}>{item.label}</span>
          </button>
        );
      })}
    </div>
  </nav>
);
