import React from 'react';
import { ChevronRight } from 'lucide-react';
import { type Subject } from '../lib/db';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';

interface SubjectCardProps {
  subject: Subject;
  onClick?: () => void;
}

/** One course as a row; place inside a `.list` surface. */
export const SubjectCard: React.FC<SubjectCardProps> = ({ subject, onClick }) => {
  const { tasks } = useTasks();
  const { notes } = useNotes();
  const key = subject.name.toLowerCase();
  const openTasks = tasks.filter(t => !t.completed && t.subject.toLowerCase() === key).length;
  const noteCount = notes.filter(n => n.subject.toLowerCase() === key).length;
  const details = [subject.code, subject.faculty].filter(Boolean).join(', ');

  return (
    <button type="button" className="list-row" onClick={onClick} style={{ minHeight: 64 }}>
      <span className="subject-mark" style={{ background: subject.color, width: 4 }} />
      <span className="grow">
        <span className="row-title truncate" style={{ display: 'block' }}>{subject.name}</span>
        <span className="meta truncate" style={{ display: 'block' }}>
          {details || `${openTasks} open ${openTasks === 1 ? 'task' : 'tasks'}, ${noteCount} ${noteCount === 1 ? 'note' : 'notes'}`}
        </span>
      </span>
      {details && (
        <span className="meta tnum" style={{ textAlign: 'right', fontSize: 12, flexShrink: 0 }}>
          {openTasks} open
        </span>
      )}
      <ChevronRight size={18} color="var(--text-3)" />
    </button>
  );
};
