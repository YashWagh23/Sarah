import React from 'react';
import { 
  Pin, 
  Trash2, 
  Calendar 
} from 'lucide-react';
import { type AcademicNote } from '../lib/db';
import { useSubjects } from '../context/SubjectsContext';

interface NoteCardProps {
  note: AcademicNote;
  onEdit: (note: AcademicNote) => void;
  onTogglePin: (id: string) => void;
  onDelete?: (id: string) => void;
  compact?: boolean;
}

export const NoteCard: React.FC<NoteCardProps> = ({
  note,
  onEdit,
  onTogglePin,
  onDelete,
  compact = false
}) => {
  const { getSubjectColor } = useSubjects();
  const subjectColor = getSubjectColor(note.subject);

  const formatTimestamp = (timestamp: number) => {
    const d = new Date(timestamp);
    const now = new Date();
    const isToday = d.toDateString() === now.toDateString();
    
    if (isToday) {
      return `Today, ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    }
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  return (
    <div
      className="surface-card btn-press"
      style={{
        padding: compact ? '12px 14px' : '15px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '9px',
        cursor: 'pointer',
        backgroundColor: note.pinned ? 'rgba(255, 255, 255, 0.96)' : '#FFFFFF',
        border: note.pinned ? '1px solid rgba(245, 158, 11, 0.35)' : '1px solid var(--sarah-outline-variant)',
        boxShadow: note.pinned ? '0 4px 16px rgba(245, 158, 11, 0.08)' : 'var(--card-shadow)',
        position: 'relative',
        transition: 'all 0.16s ease'
      }}
      onClick={() => onEdit(note)}
    >
      {/* Top Header: Subject tag + Pin indicator + Delete action */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span
            style={{
              width: '7px',
              height: '7px',
              borderRadius: '50%',
              backgroundColor: subjectColor,
              flexShrink: 0
            }}
          />
          <span
            style={{
              fontSize: '11px',
              fontWeight: 700,
              color: subjectColor,
              letterSpacing: '0.02em',
              textTransform: 'uppercase'
            }}
          >
            {note.subject}
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          {/* Pin Button */}
          <button
            type="button"
            aria-label={note.pinned ? 'Unpin note' : 'Pin note to top'}
            onClick={(e) => {
              e.stopPropagation();
              onTogglePin(note.id);
            }}
            style={{
              background: note.pinned ? 'rgba(245, 158, 11, 0.12)' : 'transparent',
              border: 'none',
              borderRadius: '8px',
              padding: '4px 7px',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              cursor: 'pointer',
              color: note.pinned ? 'var(--sarah-tertiary)' : 'var(--sarah-outline)',
              outline: 'none',
              transition: 'all 0.15s ease'
            }}
          >
            <Pin 
              size={14} 
              fill={note.pinned ? 'currentColor' : 'none'} 
              strokeWidth={note.pinned ? 2.2 : 1.8} 
              style={{ transform: note.pinned ? 'rotate(45deg)' : 'none' }}
            />
            {note.pinned && (
              <span style={{ fontSize: '10px', fontWeight: 700 }}>PINNED</span>
            )}
          </button>

          {/* Delete Button */}
          {onDelete && (
            <button
              type="button"
              aria-label="Delete note"
              onClick={(e) => {
                e.stopPropagation();
                onDelete(note.id);
              }}
              style={{
                background: 'none',
                border: 'none',
                padding: '4px 6px',
                cursor: 'pointer',
                color: 'var(--sarah-outline)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderRadius: '8px',
                outline: 'none'
              }}
            >
              <Trash2 size={14} />
            </button>
          )}
        </div>
      </div>

      {/* Note Title */}
      <h3
        style={{
          fontSize: compact ? '14px' : '15.5px',
          fontWeight: 700,
          color: 'var(--sarah-on-background)',
          margin: 0,
          lineHeight: 1.3,
          letterSpacing: '-0.01em'
        }}
      >
        {note.title}
      </h3>

      {/* Note Content Preview */}
      <p
        style={{
          fontSize: '12.5px',
          color: 'var(--sarah-on-surface-variant)',
          margin: 0,
          lineHeight: 1.45,
          display: '-webkit-box',
          WebkitLineClamp: compact ? 2 : 3,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        }}
      >
        {note.content}
      </p>

      {/* Timestamp Footer */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '4px',
          fontSize: '11px',
          color: 'var(--sarah-secondary)',
          marginTop: '2px'
        }}
      >
        <Calendar size={11} />
        <span>{formatTimestamp(note.updatedAt || note.createdAt)}</span>
      </div>
    </div>
  );
};
