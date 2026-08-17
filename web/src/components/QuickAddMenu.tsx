import React from 'react';
import { 
  Plus, 
  CheckSquare, 
  FileText, 
  Bell 
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';

export const QuickAddMenu: React.FC = () => {
  const { isQuickAddOpen, openQuickAdd, closeQuickAdd, openCreateTaskModal, showToast } = useTasks();

  const handleCreateTask = () => {
    closeQuickAdd();
    openCreateTaskModal();
  };

  const handlePlaceholderNote = () => {
    closeQuickAdd();
    showToast('📝 Note capture coming in Milestone 3');
  };

  const handlePlaceholderReminder = () => {
    closeQuickAdd();
    showToast('⏰ Reminder scheduling coming in Milestone 3');
  };

  return (
    <>
      {/* Backdrop overlay when open */}
      {isQuickAddOpen && (
        <div
          onClick={closeQuickAdd}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.35)',
            backdropFilter: 'blur(4px)',
            WebkitBackdropFilter: 'blur(4px)',
            zIndex: 80,
            animation: 'fadeIn 0.15s ease'
          }}
        />
      )}

      {/* Floating Action Menu popup */}
      {isQuickAddOpen && (
        <div
          className="glass-card"
          style={{
            position: 'fixed',
            right: '20px',
            bottom: '136px',
            width: '210px',
            padding: '8px',
            display: 'flex',
            flexDirection: 'column',
            gap: '4px',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            zIndex: 90,
            boxShadow: '0 12px 36px rgba(0, 0, 0, 0.15)',
            borderRadius: '20px',
            animation: 'slideUp 0.18s cubic-bezier(0.16, 1, 0.3, 1) forwards'
          }}
        >
          {/* Action 1: Task */}
          <button
            type="button"
            onClick={handleCreateTask}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '10px 12px',
              borderRadius: '12px',
              border: 'none',
              background: 'rgba(68, 80, 183, 0.08)',
              color: 'var(--sarah-primary)',
              fontSize: '13.5px',
              fontWeight: 700,
              cursor: 'pointer',
              textAlign: 'left'
            }}
          >
            <div
              style={{
                width: '28px',
                height: '28px',
                borderRadius: '8px',
                backgroundColor: 'var(--sarah-primary)',
                color: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <CheckSquare size={16} />
            </div>
            <span>New Task</span>
          </button>

          {/* Action 2: Note (Placeholder) */}
          <button
            type="button"
            onClick={handlePlaceholderNote}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '10px 12px',
              borderRadius: '12px',
              border: 'none',
              background: 'transparent',
              color: 'var(--sarah-on-background)',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer',
              textAlign: 'left'
            }}
          >
            <div
              style={{
                width: '28px',
                height: '28px',
                borderRadius: '8px',
                backgroundColor: 'var(--sarah-surface-container-high)',
                color: 'var(--sarah-secondary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <FileText size={16} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span>New Note</span>
              <span style={{ fontSize: '9.5px', color: 'var(--sarah-secondary)', fontWeight: 400 }}>Milestone 3</span>
            </div>
          </button>

          {/* Action 3: Reminder (Placeholder) */}
          <button
            type="button"
            onClick={handlePlaceholderReminder}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '10px 12px',
              borderRadius: '12px',
              border: 'none',
              background: 'transparent',
              color: 'var(--sarah-on-background)',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer',
              textAlign: 'left'
            }}
          >
            <div
              style={{
                width: '28px',
                height: '28px',
                borderRadius: '8px',
                backgroundColor: 'var(--sarah-surface-container-high)',
                color: 'var(--sarah-secondary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <Bell size={16} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span>Reminder</span>
              <span style={{ fontSize: '9.5px', color: 'var(--sarah-secondary)', fontWeight: 400 }}>Milestone 3</span>
            </div>
          </button>
        </div>
      )}

      {/* Main Floating Button */}
      <button
        aria-label={isQuickAddOpen ? 'Close Quick Add' : 'Open Quick Add'}
        onClick={isQuickAddOpen ? closeQuickAdd : openQuickAdd}
        className="btn-press"
        style={{
          position: 'fixed',
          right: '20px',
          bottom: '76px',
          width: '50px',
          height: '50px',
          borderRadius: '18px',
          backgroundColor: isQuickAddOpen ? 'var(--sarah-secondary)' : 'var(--sarah-primary-container)',
          color: '#FFFFFF',
          border: 'none',
          boxShadow: '0 6px 20px rgba(94, 106, 210, 0.45)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          zIndex: 85,
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          transform: isQuickAddOpen ? 'rotate(45deg)' : 'none'
        }}
      >
        <Plus size={26} strokeWidth={2.4} />
      </button>
    </>
  );
};
