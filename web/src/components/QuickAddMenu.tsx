import React, { useEffect } from 'react';
import { 
  Plus, 
  CheckSquare, 
  FileText, 
  Bell 
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';

export const QuickAddMenu: React.FC = () => {
  const { isQuickAddOpen, openQuickAdd, closeQuickAdd, openCreateTaskModal } = useTasks();
  const { openCreateNoteModal } = useNotes();
  const { openCreateReminderModal } = useReminders();

  useEffect(() => {
    if (isQuickAddOpen) {
      const original = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      return () => {
        document.body.style.overflow = original;
      };
    }
  }, [isQuickAddOpen]);

  const handleCreateTask = () => {
    closeQuickAdd();
    openCreateTaskModal();
  };

  const handleCaptureNote = () => {
    closeQuickAdd();
    openCreateNoteModal();
  };

  const handleSetReminder = () => {
    closeQuickAdd();
    openCreateReminderModal();
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

          {/* Action 2: Note */}
          <button
            type="button"
            onClick={handleCaptureNote}
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
                backgroundColor: 'rgba(245, 158, 11, 0.12)',
                color: 'var(--sarah-tertiary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <FileText size={16} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span>Capture Note</span>
              <span style={{ fontSize: '9.5px', color: 'var(--sarah-secondary)', fontWeight: 400 }}>Classroom Notes</span>
            </div>
          </button>

          {/* Action 3: Reminder */}
          <button
            type="button"
            onClick={handleSetReminder}
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
                backgroundColor: 'rgba(68, 80, 183, 0.12)',
                color: 'var(--sarah-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <Bell size={16} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span>Set Reminder</span>
              <span style={{ fontSize: '9.5px', color: 'var(--sarah-secondary)', fontWeight: 400 }}>Time Alert</span>
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
