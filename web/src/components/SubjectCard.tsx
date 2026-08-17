import React from 'react';
import { 
  CheckSquare, 
  FileText, 
  Bell, 
  ChevronRight, 
  Edit3 
} from 'lucide-react';
import { type Subject } from '../lib/db';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';

interface SubjectCardProps {
  subject: Subject;
  onClick?: () => void;
  onEdit?: () => void;
}

export const SubjectCard: React.FC<SubjectCardProps> = ({
  subject,
  onClick,
  onEdit
}) => {
  const { tasks } = useTasks();
  const { notes } = useNotes();
  const { activeReminders } = useReminders();

  const activeTaskCount = tasks.filter(
    t => !t.completed && t.subject.toLowerCase() === subject.name.toLowerCase()
  ).length;

  const noteCount = notes.filter(
    n => n.subject.toLowerCase() === subject.name.toLowerCase()
  ).length;

  const reminderCount = activeReminders.filter(
    r => (r.subject && r.subject.toLowerCase() === subject.name.toLowerCase()) ||
         (r.taskId && tasks.find(t => t.id === r.taskId)?.subject.toLowerCase() === subject.name.toLowerCase())
  ).length;

  return (
    <div
      className="surface-card btn-press"
      onClick={onClick}
      style={{
        padding: '16px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
        backgroundColor: '#FFFFFF',
        position: 'relative',
        cursor: 'pointer',
        borderLeft: `4px solid ${subject.color}`,
        transition: 'all 0.15s ease'
      }}
    >
      {/* Top row: Name, Code, Edit Button */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '8px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span
              style={{
                width: '10px',
                height: '10px',
                borderRadius: '50%',
                backgroundColor: subject.color,
                flexShrink: 0
              }}
            />
            <h3
              style={{
                fontSize: '16px',
                fontWeight: 700,
                color: 'var(--sarah-on-background)',
                margin: 0,
                letterSpacing: '-0.01em'
              }}
            >
              {subject.name}
            </h3>
          </div>

          {subject.code && (
            <span
              style={{
                fontSize: '11.5px',
                fontWeight: 600,
                color: 'var(--sarah-secondary)',
                letterSpacing: '0.04em',
                paddingLeft: '18px'
              }}
            >
              {subject.code}
            </span>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          {onEdit && (
            <button
              type="button"
              aria-label={`Edit ${subject.name}`}
              onClick={(e) => {
                e.stopPropagation();
                onEdit();
              }}
              style={{
                background: 'var(--sarah-surface-container-low)',
                border: 'none',
                borderRadius: '8px',
                width: '28px',
                height: '28px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                color: 'var(--sarah-secondary)'
              }}
            >
              <Edit3 size={13} />
            </button>
          )}
          <ChevronRight size={16} color="var(--sarah-outline)" />
        </div>
      </div>

      {/* Metrics Row */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '14px',
          paddingTop: '4px',
          borderTop: '1px solid var(--sarah-surface-container-high)',
          fontSize: '12px',
          color: 'var(--sarah-on-surface-variant)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <CheckSquare size={13} color="var(--sarah-primary)" />
          <span style={{ fontWeight: 600, color: 'var(--sarah-on-background)' }}>
            {activeTaskCount}
          </span>
          <span style={{ color: 'var(--sarah-secondary)', fontSize: '11px' }}>tasks</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <FileText size={13} color="var(--sarah-tertiary)" />
          <span style={{ fontWeight: 600, color: 'var(--sarah-on-background)' }}>
            {noteCount}
          </span>
          <span style={{ color: 'var(--sarah-secondary)', fontSize: '11px' }}>notes</span>
        </div>

        {reminderCount > 0 && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Bell size={13} color="#BA1A1A" />
            <span style={{ fontWeight: 600, color: 'var(--sarah-on-background)' }}>
              {reminderCount}
            </span>
            <span style={{ color: 'var(--sarah-secondary)', fontSize: '11px' }}>reminders</span>
          </div>
        )}
      </div>
    </div>
  );
};
