import React, { useEffect, useRef, useState } from 'react';
import { type TaskPriority } from '../lib/db';
import { addDaysStr, formatMinutes, todayStr, tomorrowStr } from '../lib/datetime';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';
import { DeleteConfirm, Field, FieldGroup, FormError, Segmented, Sheet } from './ui';
import { SubjectPicker } from './SubjectPicker';

const DURATIONS = [15, 30, 45, 60, 90, 120];

export const TaskModal: React.FC = () => {
  const { isTaskModalOpen, editingTask, initialTaskSubject, closeTaskModal, createTask, modifyTask, removeTask } = useTasks();
  const { subjects, getSubjectColor, createSubject } = useSubjects();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [subject, setSubject] = useState('General');
  const [customSubject, setCustomSubject] = useState('');
  const [isCustomSubject, setIsCustomSubject] = useState(false);
  const [deadline, setDeadline] = useState(todayStr());
  const [deadlineTime, setDeadlineTime] = useState('23:59');
  const [priority, setPriority] = useState<TaskPriority>('should');
  const [estimatedMinutes, setEstimatedMinutes] = useState(45);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const wasOpenRef = useRef(false);

  // Seeded only as the sheet opens. Re-seeding on `subjects` would wipe what the
  // student typed the moment a subject is auto-created during save.
  useEffect(() => {
    if (!isTaskModalOpen) {
      wasOpenRef.current = false;
      return;
    }
    if (wasOpenRef.current) return;
    wasOpenRef.current = true;

    const names = subjects.map(s => s.name);
    if (editingTask) {
      setTitle(editingTask.title);
      setDescription(editingTask.description || '');
      setSubject(editingTask.subject || 'General');
      setIsCustomSubject(false);
      setCustomSubject('');
      setDeadline(editingTask.deadline || todayStr());
      setDeadlineTime(editingTask.deadlineTime || '23:59');
      setPriority(editingTask.priority);
      setEstimatedMinutes(editingTask.estimatedMinutes);
    } else {
      setTitle('');
      setDescription('');
      setSubject(initialTaskSubject || names[0] || 'General');
      setIsCustomSubject(false);
      setCustomSubject('');
      setDeadline(todayStr());
      setDeadlineTime('23:59');
      setPriority('should');
      setEstimatedMinutes(45);
    }
    setErrorMessage('');
  }, [editingTask, isTaskModalOpen, subjects, initialTaskSubject]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Give the task a title.');
      return;
    }
    if (!deadline) {
      setErrorMessage('Pick a due date.');
      return;
    }

    const finalSubject = isCustomSubject ? (customSubject.trim() || 'General') : subject;
    setIsSubmitting(true);
    try {
      // A newly typed subject becomes a real subject so it gets a colour and a page.
      const exists = subjects.some(s => s.name.toLowerCase() === finalSubject.toLowerCase());
      if (!exists && finalSubject.toLowerCase() !== 'general') {
        try {
          await createSubject({ name: finalSubject, color: getSubjectColor(finalSubject) });
        } catch (subErr) {
          console.warn('Failed to auto-create subject:', subErr);
        }
      }

      const fields = {
        title: title.trim(),
        description: description.trim() || undefined,
        subject: finalSubject,
        deadline,
        deadlineTime: deadlineTime || undefined,
        priority,
        estimatedMinutes: Math.max(5, Number(estimatedMinutes) || 30)
      };
      if (editingTask) await modifyTask({ ...editingTask, ...fields });
      else await createTask({ ...fields, completed: false });
      closeTaskModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Could not save the task. Try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingTask) return;
    setIsSubmitting(true);
    try {
      await removeTask(editingTask.id);
      closeTaskModal();
    } finally {
      setIsSubmitting(false);
    }
  };

  const presets = [
    { label: 'Today', value: todayStr() },
    { label: 'Tomorrow', value: tomorrowStr() },
    { label: 'In a week', value: addDaysStr(7) }
  ];

  return (
    <Sheet
      open={isTaskModalOpen}
      title={editingTask ? 'Edit task' : 'New task'}
      onClose={closeTaskModal}
      onSubmit={handleSubmit}
      footer={(
        <>
          <button type="submit" className="btn btn-primary btn-lg" disabled={isSubmitting}>
            {editingTask ? 'Save changes' : 'Add task'}
          </button>
          {editingTask && (
            <DeleteConfirm
              label="Delete task"
              confirmText="Delete this task? Reminders linked to it stay, but lose the link."
              onConfirm={handleDelete}
              disabled={isSubmitting}
            />
          )}
        </>
      )}
    >
      <FormError message={errorMessage} />

      <Field label="What needs doing">
        <input
          className="input"
          autoFocus={!editingTask}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Finish the DBMS lab record"
        />
      </Field>

      <SubjectPicker
        value={subject}
        isCustom={isCustomSubject}
        customValue={customSubject}
        onPick={(name) => { setSubject(name); setIsCustomSubject(false); }}
        onCustom={() => setIsCustomSubject(true)}
        onCustomChange={setCustomSubject}
      />

      <FieldGroup label="Due">
        <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
          {presets.map(p => (
            <button key={p.label} type="button" className="chip" aria-pressed={deadline === p.value} onClick={() => setDeadline(p.value)}>
              {p.label}
            </button>
          ))}
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 8 }}>
          <input className="input" type="date" aria-label="Due date" value={deadline} onChange={(e) => setDeadline(e.target.value)} />
          <input className="input" type="time" aria-label="Due time" value={deadlineTime} onChange={(e) => setDeadlineTime(e.target.value)} />
        </div>
      </FieldGroup>

      <FieldGroup label={`Time needed: ${formatMinutes(estimatedMinutes)}`} hint="Sarah uses this to fit the task into your evening.">
        <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
          {DURATIONS.map(m => (
            <button key={m} type="button" className="chip tnum" aria-pressed={estimatedMinutes === m} onClick={() => setEstimatedMinutes(m)}>
              {formatMinutes(m)}
            </button>
          ))}
        </div>
      </FieldGroup>

      <FieldGroup label="Priority">
        <Segmented<TaskPriority>
          label="Priority"
          value={priority}
          onChange={setPriority}
          options={[
            { id: 'must', label: 'Must do' },
            { id: 'should', label: 'Should do' },
            { id: 'later', label: 'Later' }
          ]}
        />
      </FieldGroup>

      <Field label="Notes">
        <textarea
          className="input"
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Chapter 4, exercises 4.1 to 4.5"
        />
      </Field>
    </Sheet>
  );
};
