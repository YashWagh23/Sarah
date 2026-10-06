import React from 'react';
import { Check, Clock } from 'lucide-react';
import { type Task } from '../lib/db';
import { useSubjects } from '../context/SubjectsContext';
import { formatMinutes, todayStr, tomorrowStr } from '../lib/datetime';
import { formatClock, taskDueMs } from '../lib/planner';

interface TaskCardProps {
  task: Task;
  onToggle: (id: string) => void;
  onEdit: (task: Task) => void;
  showDate?: boolean;
  /** Replaces the due-date text, e.g. the planner's "Overdue by 2 days". */
  reason?: string;
}

export function formatDue(task: Task): string {
  const dueMs = taskDueMs(task);
  const time = task.deadlineTime && task.deadlineTime !== '23:59' ? ` ${formatClock(dueMs)}` : '';
  if (task.deadline === todayStr()) return `Today${time}`;
  if (task.deadline === tomorrowStr()) return `Tomorrow${time}`;
  return new Date(dueMs).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }) + time;
}

/** One task as a row; place inside a `.list` surface. */
export const TaskCard: React.FC<TaskCardProps> = ({ task, onToggle, onEdit, showDate = false, reason }) => {
  const { getSubjectColor } = useSubjects();
  const isOverdue = !task.completed && Boolean(task.deadline) && taskDueMs(task) < Date.now();
  const dueText = reason ?? (isOverdue ? `Overdue, ${formatDue(task)}` : formatDue(task));

  return (
    <div
      role="button"
      tabIndex={0}
      className="list-row"
      onClick={() => onEdit(task)}
      onKeyDown={(e) => {
        if (e.key === 'Enter') onEdit(task);
      }}
      style={{ cursor: 'pointer' }}
    >
      <span className="check-hit">
        <button
          type="button"
          role="checkbox"
          aria-checked={task.completed}
          aria-label={task.completed ? `Reopen "${task.title}"` : `Complete "${task.title}"`}
          className="check"
          onClick={(e) => {
            e.stopPropagation();
            onToggle(task.id);
          }}
        >
          <Check size={14} strokeWidth={3} />
        </button>
      </span>

      <div className="grow">
        <div
          className="row-title truncate"
          style={{
            color: task.completed ? 'var(--text-3)' : 'var(--text)',
            textDecoration: task.completed ? 'line-through' : 'none'
          }}
        >
          {task.title}
        </div>
        <div className="row" style={{ gap: 10, marginTop: 2, flexWrap: 'wrap' }}>
          <span className="row meta" style={{ gap: 6 }}>
            <span className="swatch" style={{ background: getSubjectColor(task.subject) }} />
            {task.subject}
          </span>
          {(showDate || isOverdue || reason) && !task.completed && (
            <span className="meta" style={isOverdue ? { color: 'var(--danger)', fontWeight: 550 } : undefined}>
              {dueText}
            </span>
          )}
        </div>
      </div>

      <div className="stack" style={{ alignItems: 'flex-end', gap: 4, flexShrink: 0 }}>
        {!task.completed && task.priority === 'must' && <span className="tag tag-danger">Must</span>}
        <span className="row meta mono" style={{ gap: 4, fontSize: 12 }}>
          <Clock size={12} />
          {formatMinutes(task.estimatedMinutes)}
        </span>
      </div>
    </div>
  );
};
