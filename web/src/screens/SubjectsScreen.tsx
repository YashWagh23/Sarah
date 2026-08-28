import React, { useState } from 'react';
import { 
  Plus, 
  BookOpen, 
  ArrowLeft, 
  Edit3, 
  CheckSquare, 
  FileText, 
  Bell 
} from 'lucide-react';
import { useSubjects } from '../context/SubjectsContext';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';
import { SubjectCard } from '../components/SubjectCard';
import { TaskCard } from '../components/TaskCard';
import { NoteCard } from '../components/NoteCard';
import { ReminderCard } from '../components/ReminderCard';

export const SubjectsScreen: React.FC = () => {
  const { 
    subjects, 
    openCreateSubjectModal, 
    openEditSubjectModal 
  } = useSubjects();

  const { 
    tasks, 
    toggleTaskCompletion, 
    openEditTaskModal, 
    openCreateTaskModal 
  } = useTasks();

  const { 
    notes, 
    openEditNoteModal, 
    openCreateNoteModal, 
    togglePin, 
    removeNote 
  } = useNotes();

  const { 
    activeReminders, 
    openEditReminderModal, 
    openCreateReminderModal, 
    dismiss, 
    snooze, 
    removeReminder 
  } = useReminders();

  const [selectedSubjectId, setSelectedSubjectId] = useState<string | null>(null);
  const selectedSubject = subjects.find(s => s.id === selectedSubjectId) || null;

  // If a subject is selected for detail view
  if (selectedSubject) {
    const subjectTasks = tasks.filter(
      t => t.subject.toLowerCase() === selectedSubject.name.toLowerCase()
    );
    const subjectActiveTasks = subjectTasks.filter(t => !t.completed);
    const subjectCompletedTasks = subjectTasks.filter(t => t.completed);

    const subjectNotes = notes.filter(
      n => n.subject.toLowerCase() === selectedSubject.name.toLowerCase()
    );

    const subjectReminders = activeReminders.filter(
      r => (r.subject && r.subject.toLowerCase() === selectedSubject.name.toLowerCase()) ||
           (r.taskId && tasks.find(t => t.id === r.taskId)?.subject.toLowerCase() === selectedSubject.name.toLowerCase())
    );

    return (
      <div
        className="animate-fade-in"
        style={{
          padding: '16px 18px 90px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '18px'
        }}
      >
        {/* Navigation & Header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <button
            type="button"
            onClick={() => setSelectedSubjectId(null)}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 12px',
              fontSize: '13px',
              fontWeight: 600,
              color: 'var(--sarah-on-background)',
              cursor: 'pointer'
            }}
          >
            <ArrowLeft size={16} />
            <span>All Courses</span>
          </button>

          <button
            type="button"
            onClick={() => openEditSubjectModal(selectedSubject)}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              background: 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 12px',
              fontSize: '13px',
              fontWeight: 600,
              color: 'var(--sarah-secondary)',
              cursor: 'pointer'
            }}
          >
            <Edit3 size={14} />
            <span>Edit</span>
          </button>
        </div>

        {/* Subject Header Banner */}
        <div
          className="surface-card"
          style={{
            padding: '18px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            borderLeft: `5px solid ${selectedSubject.color}`,
            backgroundColor: '#FFFFFF'
          }}
        >
          <div
            style={{
              width: '46px',
              height: '46px',
              borderRadius: '14px',
              backgroundColor: `${selectedSubject.color}18`,
              color: selectedSubject.color,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}
          >
            <BookOpen size={24} />
          </div>
          <div>
            <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0, letterSpacing: '-0.02em' }}>
              {selectedSubject.name}
            </h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '2px' }}>
              {selectedSubject.code && (
                <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--sarah-secondary)', letterSpacing: '0.04em' }}>
                  {selectedSubject.code}
                </span>
              )}
              <span style={{ fontSize: '11px', color: 'var(--sarah-outline)' }}>•</span>
              <span style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)' }}>
                {subjectActiveTasks.length} pending tasks • {subjectNotes.length} notes
              </span>
            </div>
          </div>
        </div>

        {/* Section 1: Tasks for Subject */}
        <section style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CheckSquare size={14} color="var(--sarah-primary)" />
              <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--sarah-primary)' }}>
                Course Tasks ({subjectTasks.length})
              </span>
            </div>

            <button
              type="button"
              onClick={() => openCreateTaskModal()}
              style={{
                background: 'none',
                border: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '3px',
                fontSize: '11.5px',
                fontWeight: 600,
                color: 'var(--sarah-primary)',
                cursor: 'pointer',
                padding: '2px 4px'
              }}
            >
              <Plus size={13} />
              <span>Add Task</span>
            </button>
          </div>

          {subjectActiveTasks.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {subjectActiveTasks.map((t) => (
                <TaskCard
                  key={t.id}
                  task={t}
                  onToggle={toggleTaskCompletion}
                  onEdit={openEditTaskModal}
                />
              ))}
            </div>
          ) : (
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-secondary)', backgroundColor: 'var(--sarah-surface-container-lowest)', padding: '12px 14px', borderRadius: '12px', textAlign: 'center' }}>
              No pending tasks for this course.
            </div>
          )}

          {subjectCompletedTasks.length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '4px' }}>
              <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase', paddingLeft: '4px' }}>
                Completed ({subjectCompletedTasks.length})
              </span>
              {subjectCompletedTasks.map((t) => (
                <TaskCard
                  key={t.id}
                  task={t}
                  onToggle={toggleTaskCompletion}
                  onEdit={openEditTaskModal}
                />
              ))}
            </div>
          )}
        </section>

        {/* Section 2: Academic Notes for Subject */}
        <section style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <FileText size={14} color="var(--sarah-tertiary)" />
              <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--sarah-tertiary)' }}>
                Course Notes ({subjectNotes.length})
              </span>
            </div>

            <button
              type="button"
              onClick={() => openCreateNoteModal(selectedSubject.name)}
              style={{
                background: 'none',
                border: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '3px',
                fontSize: '11.5px',
                fontWeight: 600,
                color: 'var(--sarah-primary)',
                cursor: 'pointer',
                padding: '2px 4px'
              }}
            >
              <Plus size={13} />
              <span>Capture Note</span>
            </button>
          </div>

          {subjectNotes.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {subjectNotes.map((note) => (
                <NoteCard
                  key={note.id}
                  note={note}
                  onEdit={openEditNoteModal}
                  onTogglePin={togglePin}
                  onDelete={removeNote}
                />
              ))}
            </div>
          ) : (
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-secondary)', backgroundColor: 'var(--sarah-surface-container-lowest)', padding: '12px 14px', borderRadius: '12px', textAlign: 'center' }}>
              No academic notes for this course yet.
            </div>
          )}
        </section>

        {/* Section 3: Active Reminders for Subject */}
        {subjectReminders.length > 0 && (
          <section style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Bell size={14} color="var(--sarah-primary)" />
                <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--sarah-primary)' }}>
                  Active Reminders ({subjectReminders.length})
                </span>
              </div>

              <button
                type="button"
                onClick={() => openCreateReminderModal()}
                style={{
                  background: 'none',
                  border: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '3px',
                  fontSize: '11.5px',
                  fontWeight: 600,
                  color: 'var(--sarah-primary)',
                  cursor: 'pointer',
                  padding: '2px 4px'
                }}
              >
                <Plus size={13} />
                <span>Add Reminder</span>
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {subjectReminders.map((reminder) => (
                <ReminderCard
                  key={reminder.id}
                  reminder={reminder}
                  onDismiss={dismiss}
                  onSnooze={snooze}
                  onEdit={openEditReminderModal}
                  onDelete={removeReminder}
                />
              ))}
            </div>
          </section>
        )}
      </div>
    );
  }

  // Main Subjects List View
  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px'
      }}
    >
      {/* Top Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0, letterSpacing: '-0.02em' }}>
            Subjects
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
            Keep your college life organized by course.
          </p>
        </div>
        <button
          type="button"
          onClick={() => openCreateSubjectModal()}
          className="btn-press"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            background: 'var(--sarah-primary)',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '12px',
            padding: '8px 14px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            boxShadow: '0 3px 10px rgba(68, 80, 183, 0.28)'
          }}
        >
          <Plus size={15} strokeWidth={2.5} />
          <span>New Subject</span>
        </button>
      </div>

      {/* Course Cards Grid/List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {subjects.map((subject) => (
          <SubjectCard
            key={subject.id}
            subject={subject}
            onClick={() => setSelectedSubjectId(subject.id)}
            onEdit={() => openEditSubjectModal(subject)}
          />
        ))}
      </div>

      {subjects.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: '40px 10px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px'
          }}
        >
          <div
            style={{
              width: '52px',
              height: '52px',
              borderRadius: '16px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-secondary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <BookOpen size={24} />
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              No subjects yet.
            </div>
            <div style={{ fontSize: '12px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
              Add your courses to organize tasks, academic notes, and reminders.
            </div>
          </div>
          <button
            type="button"
            onClick={() => openCreateSubjectModal()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'var(--sarah-primary)',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 16px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <Plus size={15} />
            <span>New Subject</span>
          </button>
        </div>
      )}
    </div>
  );
};
