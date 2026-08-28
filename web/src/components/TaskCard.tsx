import React from 'react';
import { 
  CheckCircle2, 
  Circle, 
  Clock, 
  Flame, 
  BookOpen, 
  Calendar,
  Trash2
} from 'lucide-react';
import { type Task } from '../lib/db';
import { useSubjects } from '../context/SubjectsContext';

interface TaskCardProps {
  task: Task;
  onToggle: (id: string) => void;
  onEdit: (task: Task) => void;
  onDelete?: (id: string) => void;
  showDate?: boolean;
}

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onToggle,
  onEdit,
  onDelete,
  showDate = false
}) => {
  const { getSubjectColor } = useSubjects();
  const subjectColor = getSubjectColor(task.subject);

  const formatDeadline = (dateStr: string, timeStr?: string) => {
    const todayStr = new Date().toISOString().split('T')[0];
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];

    let dateLabel = dateStr;
    if (dateStr === todayStr) {
      dateLabel = 'Today';
    } else if (dateStr === tomorrowStr) {
      dateLabel = 'Tomorrow';
    } else {
      const parts = dateStr.split('-');
      if (parts.length === 3) {
        const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
        dateLabel = d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      }
    }

    if (timeStr) {
      return `${dateLabel} at ${timeStr}`;
    }
    return dateLabel;
  };

  return (
    <div
      className="surface-card btn-press"
      style={{
        padding: '13px 15px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        cursor: 'pointer',
        transition: 'all 0.18s ease',
        opacity: task.completed ? 0.6 : 1,
        backgroundColor: task.completed ? 'rgba(255, 255, 255, 0.65)' : '#FFFFFF',
        position: 'relative'
      }}
      onClick={() => onEdit(task)}
    >
      {/* Complete Checkbox (Stops propagation) */}
      <button
        type="button"
        aria-label={task.completed ? 'Mark as incomplete' : 'Mark as completed'}
        onClick={(e) => {
          e.stopPropagation();
          onToggle(task.id);
        }}
        style={{
          background: 'none',
          border: 'none',
          padding: '4px',
          margin: '-4px',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: task.completed ? 'var(--sarah-primary)' : 'var(--sarah-outline)',
          flexShrink: 0,
          outline: 'none'
        }}
      >
        {task.completed ? (
          <CheckCircle2 size={23} fill="var(--sarah-primary-fixed)" strokeWidth={2.2} />
        ) : (
          <Circle size={23} strokeWidth={1.8} />
        )}
      </button>

      {/* Main Content */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: '14.5px',
            fontWeight: 600,
            color: 'var(--sarah-on-background)',
            textDecoration: task.completed ? 'line-through' : 'none',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            letterSpacing: '-0.01em'
          }}
        >
          {task.title}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '6px', marginTop: '3px' }}>
          {/* Subject badge */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span
              style={{
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                backgroundColor: subjectColor,
                flexShrink: 0
              }}
            />
            <span style={{ fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              {task.subject}
            </span>
          </div>

          <span style={{ fontSize: '10px', color: 'var(--sarah-outline)' }}>•</span>

          {/* Duration */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '3px', fontSize: '11px', color: 'var(--sarah-secondary)' }}>
            <Clock size={11} />
            <span>{task.estimatedMinutes}m</span>
          </div>

          {/* Optional Date */}
          {showDate && task.deadline && (
            <>
              <span style={{ fontSize: '10px', color: 'var(--sarah-outline)' }}>•</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '3px', fontSize: '11px', color: 'var(--sarah-secondary)' }}>
                <Calendar size={11} />
                <span>{formatDeadline(task.deadline, task.deadlineTime)}</span>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Priority Tag & Actions */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexShrink: 0 }}>
        {!task.completed && (
          <>
            {task.priority === 'must' && (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 700,
                  padding: '3px 8px',
                  borderRadius: '8px',
                  backgroundColor: 'rgba(186, 26, 26, 0.1)',
                  color: 'var(--sarah-error)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '3px'
                }}
              >
                <Flame size={11} /> Must do
              </span>
            )}
            {task.priority === 'should' && (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 600,
                  padding: '3px 8px',
                  borderRadius: '8px',
                  backgroundColor: 'rgba(131, 79, 0, 0.1)',
                  color: 'var(--sarah-tertiary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '3px'
                }}
              >
                <BookOpen size={11} /> Should do
              </span>
            )}
            {task.priority === 'later' && (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 500,
                  padding: '3px 8px',
                  borderRadius: '8px',
                  backgroundColor: 'var(--sarah-surface-container-high)',
                  color: 'var(--sarah-secondary)'
                }}
              >
                Later
              </span>
            )}
          </>
        )}

        {onDelete && (
          <button
            type="button"
            aria-label="Delete task"
            onClick={(e) => {
              e.stopPropagation();
              onDelete(task.id);
            }}
            style={{
              background: 'none',
              border: 'none',
              padding: '6px',
              cursor: 'pointer',
              color: 'var(--sarah-outline)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '8px',
              outline: 'none'
            }}
          >
            <Trash2 size={15} />
          </button>
        )}
      </div>
    </div>
  );
};
