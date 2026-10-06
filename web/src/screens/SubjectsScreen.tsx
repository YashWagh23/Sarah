import React, { useState } from 'react';
import { ArrowLeft, ChevronDown, ChevronUp, Pencil, Plus } from 'lucide-react';
import { useSubjects } from '../context/SubjectsContext';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';
import { SubjectCard } from '../components/SubjectCard';
import { TaskCard } from '../components/TaskCard';
import { NoteCard } from '../components/NoteCard';
import { ReminderCard } from '../components/ReminderCard';
import { EmptyState, SectionHead } from '../components/ui';
import { taskDueMs } from '../lib/planner';

const AddButton: React.FC<{ label: string; onClick: () => void }> = ({ label, onClick }) => (
  <button type="button" className="btn btn-ghost btn-sm" onClick={onClick}>
    <Plus size={15} />
    {label}
  </button>
);

const InlineEmpty: React.FC<{ text: string }> = ({ text }) => (
  <p className="panel meta" style={{ padding: '14px 16px' }}>{text}</p>
);

export const SubjectsScreen: React.FC = () => {
  const { subjects, openCreateSubjectModal, openEditSubjectModal, viewingSubject, openSubjectDetail, closeSubjectDetail } = useSubjects();
  const { tasks, toggleTaskCompletion, openEditTaskModal, openCreateTaskModal } = useTasks();
  const { notes, openEditNoteModal, openCreateNoteModal, togglePin } = useNotes();
  const { activeReminders, openEditReminderModal, openCreateReminderModal, dismiss, snooze } = useReminders();
  const [showDone, setShowDone] = useState(false);

  // Detail selection lives in the subjects context so a rename or delete keeps
  // this view in sync instead of stranding it on a stale copy of the record.
  const subject = viewingSubject;

  if (subject) {
    const key = subject.name.toLowerCase();
    const subjectTasks = tasks.filter(t => t.subject.toLowerCase() === key);
    const open = subjectTasks.filter(t => !t.completed).sort((a, b) => taskDueMs(a) - taskDueMs(b));
    const done = subjectTasks.filter(t => t.completed);
    const subjectNotes = notes.filter(n => n.subject.toLowerCase() === key);
    const subjectReminders = activeReminders.filter(
      r => r.subject?.toLowerCase() === key
        || (r.taskId && tasks.find(t => t.id === r.taskId)?.subject.toLowerCase() === key)
    );
    const details = [
      subject.code,
      subject.faculty,
      subject.credits != null ? `${subject.credits} credits` : null,
      subject.weeklyHours != null ? `${subject.weeklyHours}h a week` : null
    ].filter(Boolean).join(', ');

    return (
      <div className="screen">
        <div className="row" style={{ justifyContent: 'space-between', margin: '-8px -8px -8px -8px' }}>
          <button type="button" className="btn btn-ghost" onClick={closeSubjectDetail} style={{ color: 'var(--text-2)' }}>
            <ArrowLeft size={18} />
            Subjects
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => openEditSubjectModal(subject)}>
            <Pencil size={16} />
            Edit
          </button>
        </div>

        <header className="row" style={{ gap: 14, alignItems: 'stretch' }}>
          <span className="subject-mark" style={{ width: 5, background: subject.color }} />
          <div className="grow">
            <h2 className="screen-title">{subject.name}</h2>
            <p className="screen-sub">{details || `${open.length} open, ${subjectNotes.length} notes`}</p>
          </div>
        </header>

        <section aria-labelledby="s-tasks" className="stack-8">
          <SectionHead id="s-tasks" title="Tasks" count={open.length} action={<AddButton label="Task" onClick={() => openCreateTaskModal(subject.name)} />} />
          {open.length > 0 ? (
            <div className="list">
              {open.map(t => <TaskCard key={t.id} task={t} showDate onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />)}
            </div>
          ) : (
            <InlineEmpty text="Nothing open for this subject." />
          )}
          {done.length > 0 && (
            <>
              <button type="button" className="btn btn-ghost btn-sm" style={{ alignSelf: 'flex-start', color: 'var(--text-2)' }} onClick={() => setShowDone(v => !v)}>
                {showDone ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
                {done.length} done
              </button>
              {showDone && (
                <div className="list">
                  {done.map(t => <TaskCard key={t.id} task={t} onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />)}
                </div>
              )}
            </>
          )}
        </section>

        <section aria-labelledby="s-notes" className="stack-8">
          <SectionHead id="s-notes" title="Notes" count={subjectNotes.length} action={<AddButton label="Note" onClick={() => openCreateNoteModal(subject.name)} />} />
          {subjectNotes.length > 0 ? (
            <div className="stack-8">
              {subjectNotes.map(n => <NoteCard key={n.id} note={n} onEdit={openEditNoteModal} onTogglePin={togglePin} />)}
            </div>
          ) : (
            <InlineEmpty text="No notes for this subject yet." />
          )}
        </section>

        <section aria-labelledby="s-rem" className="stack-8">
          <SectionHead
            id="s-rem"
            title="Reminders"
            count={subjectReminders.length}
            action={<AddButton label="Reminder" onClick={() => openCreateReminderModal(undefined, undefined, subject.name)} />}
          />
          {subjectReminders.length > 0 ? (
            <div className="list">
              {subjectReminders.map(r => (
                <ReminderCard key={r.id} reminder={r} onDismiss={dismiss} onSnooze={snooze} onEdit={openEditReminderModal} />
              ))}
            </div>
          ) : (
            <InlineEmpty text="No reminders for this subject." />
          )}
        </section>
      </div>
    );
  }

  return (
    <div className="screen">
      <header className="screen-head">
        <div>
          <h2 className="screen-title">Subjects</h2>
          <p className="screen-sub">{subjects.length} {subjects.length === 1 ? 'course' : 'courses'} this semester</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={openCreateSubjectModal}>
          <Plus size={16} />
          New subject
        </button>
      </header>

      {subjects.length > 0 ? (
        <div className="list">
          {subjects.map(s => <SubjectCard key={s.id} subject={s} onClick={() => openSubjectDetail(s)} />)}
        </div>
      ) : (
        <EmptyState
          title="No subjects yet"
          body="Add your courses to keep each one's tasks, notes and reminders together."
          action={(
            <button type="button" className="btn btn-primary" onClick={openCreateSubjectModal}>
              <Plus size={16} />
              New subject
            </button>
          )}
        />
      )}
    </div>
  );
};
