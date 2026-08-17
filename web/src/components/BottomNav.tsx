import React from 'react';
import { 
  Sparkles, 
  CheckSquare, 
  FileText, 
  BookOpen, 
  User 
} from 'lucide-react';

export type TabId = 'today' | 'tasks' | 'notes' | 'subjects' | 'profile';

interface NavItem {
  id: TabId;
  label: string;
  icon: React.ComponentType<{ size?: number; className?: string; strokeWidth?: number }>;
}

const NAV_ITEMS: NavItem[] = [
  { id: 'today', label: 'Today', icon: Sparkles },
  { id: 'tasks', label: 'Tasks', icon: CheckSquare },
  { id: 'notes', label: 'Notes', icon: FileText },
  { id: 'subjects', label: 'Subjects', icon: BookOpen },
  { id: 'profile', label: 'Profile', icon: User }
];

interface BottomNavProps {
  activeTab: TabId;
  onTabSelect: (tab: TabId) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ activeTab, onTabSelect }) => {
  return (
    <nav 
      aria-label="App Navigation"
      style={{
        position: 'relative',
        width: '100%',
        background: 'rgba(255, 255, 255, 0.88)',
        backdropFilter: 'blur(20px) saturate(180%)',
        WebkitBackdropFilter: 'blur(20px) saturate(180%)',
        borderTop: '1px solid rgba(226, 226, 232, 0.8)',
        zIndex: 50,
      }}
      className="safe-bottom"
    >
      <div 
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          height: '56px',
          padding: '0 8px',
          maxWidth: '540px',
          margin: '0 auto'
        }}
      >
        {NAV_ITEMS.map((item) => {
          const isSelected = activeTab === item.id;
          const Icon = item.icon;
          return (
            <button
              key={item.id}
              onClick={() => onTabSelect(item.id)}
              className="btn-press"
              style={{
                background: 'none',
                border: 'none',
                outline: 'none',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                flex: 1,
                padding: '6px 0',
                gap: '3px',
                color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-secondary)',
                transition: 'color 0.15s ease'
              }}
            >
              <div 
                style={{
                  position: 'relative',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '2px 14px',
                  borderRadius: '14px',
                  backgroundColor: isSelected ? 'rgba(68, 80, 183, 0.1)' : 'transparent',
                  transition: 'background-color 0.2s ease'
                }}
              >
                <Icon 
                  size={20} 
                  strokeWidth={isSelected ? 2.5 : 2} 
                />
              </div>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: isSelected ? 600 : 500,
                  letterSpacing: '0.01em'
                }}
              >
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
