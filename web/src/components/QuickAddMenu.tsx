import React, { useEffect } from 'react';
import { Bell, ListPlus, NotebookPen, Plus } from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';

interface QuickAddMenuProps {
  /** Hidden on settings-style screens where it would sit on top of controls. */
  hidden?: boolean;
}

export const QuickAddMenu: React.FC<QuickAddMenuProps> = ({ hidden = false }) => {
  const { isQuickAddOpen, openQuickAdd, closeQuickAdd, openCreateTaskModal } = useTasks();
  const { openCreateNoteModal } = useNotes();
  const { openCreateReminderModal } = useReminders();

  useEffect(() => {
    if (hidden && isQuickAddOpen) closeQuickAdd();
  }, [hidden, isQuickAddOpen, closeQuickAdd]);

  useEffect(() => {
    if (!isQuickAddOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') closeQuickAdd();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isQuickAddOpen, closeQuickAdd]);

  if (hidden) return null;

  const actions = [
    { label: 'Task', hint: 'Something with a deadline', icon: ListPlus, run: () => openCreateTaskModal() },
    { label: 'Note', hint: 'Lecture notes, formulas', icon: NotebookPen, run: () => openCreateNoteModal() },
    { label: 'Reminder', hint: 'A nudge at a set time', icon: Bell, run: () => openCreateReminderModal() }
  ];

  return (
    <>
      {isQuickAddOpen && (
        <div
          onClick={closeQuickAdd}
          style={{ position: 'fixed', inset: 0, zIndex: 80, background: 'var(--scrim)', animation: 'fade 0.18s ease both' }}
        />
      )}

      {isQuickAddOpen && (
        <div
          role="menu"
          aria-label="Add"
          className="list fab"
          style={{
            bottom: 'calc(140px + env(safe-area-inset-bottom, 0px))',
            width: 248,
            zIndex: 90,
            boxShadow: 'var(--shadow-2)',
            transformOrigin: 'bottom right',
            animation: 'pop 0.2s var(--ease-out) both'
          }}
        >
          {actions.map(action => {
            const Icon = action.icon;
            return (
              <button
                key={action.label}
                type="button"
                role="menuitem"
                className="list-row"
                onClick={() => {
                  closeQuickAdd();
                  action.run();
                }}
              >
                <span
                  className="row"
                  style={{
                    justifyContent: 'center',
                    width: 34,
                    height: 34,
                    borderRadius: 'var(--r-control)',
                    background: 'var(--accent-soft)',
                    color: 'var(--accent-text)',
                    flexShrink: 0
                  }}
                >
                  <Icon size={17} />
                </span>
                <span className="grow">
                  <span className="row-title" style={{ display: 'block' }}>{action.label}</span>
                  <span className="meta">{action.hint}</span>
                </span>
              </button>
            );
          })}
        </div>
      )}

      <button
        type="button"
        aria-label={isQuickAddOpen ? 'Close add menu' : 'Add task, note or reminder'}
        aria-expanded={isQuickAddOpen}
        onClick={isQuickAddOpen ? closeQuickAdd : openQuickAdd}
        className="fab press"
        style={{
          width: 52,
          height: 52,
          borderRadius: 'var(--r-surface)',
          background: 'var(--accent)',
          color: 'var(--accent-ink)',
          boxShadow: 'var(--shadow-2)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 91
        }}
      >
        <Plus
          size={24}
          style={{ transition: 'transform 0.22s var(--ease-out)', transform: isQuickAddOpen ? 'rotate(45deg)' : 'none' }}
        />
      </button>
    </>
  );
};
