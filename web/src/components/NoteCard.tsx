import React from 'react';
import { Pin } from 'lucide-react';
import { type AcademicNote } from '../lib/db';
import { useSubjects } from '../context/SubjectsContext';

interface NoteCardProps {
  note: AcademicNote;
  onEdit: (note: AcademicNote) => void;
  onTogglePin: (id: string) => void;
  compact?: boolean;
}

function formatEdited(timestamp: number): string {
  const d = new Date(timestamp);
  if (d.toDateString() === new Date().toDateString()) {
    return `Today, ${d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}`;
  }
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export const NoteCard: React.FC<NoteCardProps> = ({ note, onEdit, onTogglePin, compact = false }) => {
  const { getSubjectColor } = useSubjects();

  return (
    <article
      role="button"
      tabIndex={0}
      onClick={() => onEdit(note)}
      onKeyDown={(e) => {
        if (e.key === 'Enter') onEdit(note);
      }}
      className="card press"
      style={{ padding: compact ? '12px 14px' : '14px 16px', cursor: 'pointer', display: 'flex', flexDirection: 'column', gap: 6 }}
    >
      <div className="row" style={{ justifyContent: 'space-between', gap: 8 }}>
        <h4 className="row-title truncate grow" style={{ fontSize: 15 }}>{note.title}</h4>
        <button
          type="button"
          aria-pressed={note.pinned}
          aria-label={note.pinned ? `Unpin "${note.title}"` : `Pin "${note.title}"`}
          className="icon-btn"
          style={{ width: 32, height: 32, margin: '-6px -8px -6px 0', color: note.pinned ? 'var(--accent-text)' : 'var(--text-3)' }}
          onClick={(e) => {
            e.stopPropagation();
            onTogglePin(note.id);
          }}
        >
          <Pin size={16} fill={note.pinned ? 'currentColor' : 'none'} />
        </button>
      </div>

      <p
        className="selectable-text"
        style={{
          fontSize: 13.5,
          lineHeight: 1.5,
          color: 'var(--text-2)',
          display: '-webkit-box',
          WebkitLineClamp: compact ? 2 : 3,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word'
        }}
      >
        {note.content}
      </p>

      <div className="row" style={{ gap: 10 }}>
        <span className="row meta" style={{ gap: 6 }}>
          <span className="swatch" style={{ background: getSubjectColor(note.subject) }} />
          {note.subject}
        </span>
        <span className="meta">{formatEdited(note.updatedAt || note.createdAt)}</span>
      </div>
    </article>
  );
};
