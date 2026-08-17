import React, { useState } from 'react';
import { 
  Bell, 
  Check, 
  Clock, 
  Trash2, 
  Link as LinkIcon 
} from 'lucide-react';
import { type Reminder } from '../lib/db';
import { useTasks } from '../context/TasksContext';

interface ReminderCardProps {
  reminder: Reminder;
  onDismiss: (id: string) => void;
  onSnooze: (id: string, newTimeEpochMs: number) => void;
  onEdit: (reminder: Reminder) => void;
  onDelete?: (id: string) => void;
}

export const ReminderCard: React.FC<ReminderCardProps> = ({
  reminder,
  onDismiss,
  onSnooze,
  onEdit,
  onDelete
}) => {
  const [showSnoozeMenu, setShowSnoozeMenu] = useState(false);
  const { tasks } = useTasks();

  const linkedTask = reminder.taskId ? tasks.find(t => t.id === reminder.taskId) : null;
  const now = Date.now();
  const isOverdue = reminder.reminderAt < now;
  const isDueSoon = !isOverdue && (reminder.reminderAt - now < 3600000 * 2); // within 2h

  const formatRelativeTime = (epochMs: number) => {
    const d = new Date(epochMs);
    const today = new Date();
    const isToday = d.toDateString() === today.toDateString();
    
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const isTomorrow = d.toDateString() === tomorrow.toDateString();

    const timeStr = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    if (isOverdue) {
      const diffMinutes = Math.max(1, Math.round((now - epochMs) / 60000));
      if (diffMinutes < 60) {
        return `Overdue by ${diffMinutes}m`;
      }
      return `Overdue (${timeStr})`;
    }

    if (isToday) {
      return `Today at ${timeStr}`;
    }
    if (isTomorrow) {
      return `Tomorrow at ${timeStr}`;
    }
    return `${d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} at ${timeStr}`;
  };

  const handleSnoozeOption = (minutesToAdd: number) => {
    setShowSnoozeMenu(false);
    const newTime = Date.now() + minutesToAdd * 60000;
    onSnooze(reminder.id, newTime);
  };

  const handleSnoozeTomorrowMorning = () => {
    setShowSnoozeMenu(false);
    const tomorrow9am = new Date();
    tomorrow9am.setDate(tomorrow9am.getDate() + 1);
    tomorrow9am.setHours(9, 0, 0, 0);
    onSnooze(reminder.id, tomorrow9am.getTime());
  };

  // Status colors
  let iconBg = 'rgba(68, 80, 183, 0.08)';
  let iconColor = 'var(--sarah-primary)';
  let statusBadgeBg = 'var(--sarah-surface-container-low)';
  let statusBadgeColor = 'var(--sarah-secondary)';

  if (isOverdue) {
    iconBg = 'rgba(186, 26, 26, 0.12)';
    iconColor = 'var(--sarah-error)';
    statusBadgeBg = 'rgba(186, 26, 26, 0.1)';
    statusBadgeColor = 'var(--sarah-error)';
  } else if (isDueSoon) {
    iconBg = 'rgba(245, 158, 11, 0.12)';
    iconColor = 'var(--sarah-tertiary)';
    statusBadgeBg = 'rgba(245, 158, 11, 0.1)';
    statusBadgeColor = 'var(--sarah-tertiary)';
  }

  return (
    <div
      className="surface-card btn-press"
      style={{
        padding: '13px 15px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        cursor: 'pointer',
        backgroundColor: '#FFFFFF',
        position: 'relative',
        transition: 'all 0.16s ease'
      }}
      onClick={() => onEdit(reminder)}
    >
      {/* Bell Icon / State indicator */}
      <div
        style={{
          width: '36px',
          height: '36px',
          borderRadius: '11px',
          backgroundColor: iconBg,
          color: iconColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}
      >
        <Bell size={18} strokeWidth={isOverdue ? 2.4 : 2} />
      </div>

      {/* Main Info */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: '14.5px',
            fontWeight: 700,
            color: 'var(--sarah-on-background)',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            letterSpacing: '-0.01em'
          }}
        >
          {reminder.title}
        </div>

        {reminder.message && (
          <div
            style={{
              fontSize: '12px',
              color: 'var(--sarah-on-surface-variant)',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              marginTop: '1px'
            }}
          >
            {reminder.message}
          </div>
        )}

        <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '6px', marginTop: '3px' }}>
          {/* Time Badge */}
          <span
            style={{
              fontSize: '11px',
              fontWeight: 600,
              padding: '2px 7px',
              borderRadius: '6px',
              backgroundColor: statusBadgeBg,
              color: statusBadgeColor
            }}
          >
            {formatRelativeTime(reminder.reminderAt)}
          </span>

          {/* Optional Linked Task */}
          {linkedTask && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '3px',
                fontSize: '11px',
                color: 'var(--sarah-secondary)',
                backgroundColor: 'var(--sarah-surface-container-low)',
                padding: '2px 6px',
                borderRadius: '6px'
              }}
            >
              <LinkIcon size={10} />
              <span style={{ maxWidth: '110px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {linkedTask.title}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Action Buttons (Dismiss, Snooze, Delete) */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px', flexShrink: 0 }}>
        {/* Snooze button */}
        <div style={{ position: 'relative' }}>
          <button
            type="button"
            aria-label="Snooze reminder"
            onClick={(e) => {
              e.stopPropagation();
              setShowSnoozeMenu(prev => !prev);
            }}
            style={{
              background: 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '8px',
              padding: '6px 8px',
              cursor: 'pointer',
              color: 'var(--sarah-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: '3px',
              fontSize: '11px',
              fontWeight: 600
            }}
          >
            <Clock size={12} />
            <span>Snooze</span>
          </button>

          {/* Snooze Dropdown Menu */}
          {showSnoozeMenu && (
            <>
              <div
                onClick={(e) => {
                  e.stopPropagation();
                  setShowSnoozeMenu(false);
                }}
                style={{
                  position: 'fixed',
                  inset: 0,
                  zIndex: 70
                }}
              />
              <div
                onClick={(e) => e.stopPropagation()}
                className="glass-card"
                style={{
                  position: 'absolute',
                  right: 0,
                  top: '34px',
                  width: '140px',
                  padding: '4px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '2px',
                  backgroundColor: '#FFFFFF',
                  zIndex: 75,
                  boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)',
                  borderRadius: '12px'
                }}
              >
                <button
                  type="button"
                  onClick={() => handleSnoozeOption(10)}
                  style={{
                    border: 'none',
                    background: 'none',
                    padding: '6px 8px',
                    textAlign: 'left',
                    fontSize: '12px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    borderRadius: '6px'
                  }}
                >
                  +10 minutes
                </button>
                <button
                  type="button"
                  onClick={() => handleSnoozeOption(30)}
                  style={{
                    border: 'none',
                    background: 'none',
                    padding: '6px 8px',
                    textAlign: 'left',
                    fontSize: '12px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    borderRadius: '6px'
                  }}
                >
                  +30 minutes
                </button>
                <button
                  type="button"
                  onClick={() => handleSnoozeOption(60)}
                  style={{
                    border: 'none',
                    background: 'none',
                    padding: '6px 8px',
                    textAlign: 'left',
                    fontSize: '12px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    borderRadius: '6px'
                  }}
                >
                  +1 hour
                </button>
                <button
                  type="button"
                  onClick={handleSnoozeTomorrowMorning}
                  style={{
                    border: 'none',
                    background: 'none',
                    padding: '6px 8px',
                    textAlign: 'left',
                    fontSize: '12px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    borderRadius: '6px'
                  }}
                >
                  Tomorrow 9 AM
                </button>
              </div>
            </>
          )}
        </div>

        {/* Dismiss checkmark button */}
        <button
          type="button"
          aria-label="Dismiss reminder"
          onClick={(e) => {
            e.stopPropagation();
            onDismiss(reminder.id);
          }}
          style={{
            background: 'rgba(16, 185, 129, 0.1)',
            border: 'none',
            borderRadius: '8px',
            width: '30px',
            height: '30px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            color: '#059669',
            outline: 'none'
          }}
        >
          <Check size={16} strokeWidth={2.5} />
        </button>

        {/* Delete button if provided */}
        {onDelete && (
          <button
            type="button"
            aria-label="Delete reminder"
            onClick={(e) => {
              e.stopPropagation();
              onDelete(reminder.id);
            }}
            style={{
              background: 'none',
              border: 'none',
              padding: '4px',
              cursor: 'pointer',
              color: 'var(--sarah-outline)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '6px'
            }}
          >
            <Trash2 size={14} />
          </button>
        )}
      </div>
    </div>
  );
};
