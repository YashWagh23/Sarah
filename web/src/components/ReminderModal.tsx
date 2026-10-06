import React, { useEffect, useState } from 'react';
import { useReminders } from '../context/RemindersContext';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';
import { toLocalDateStr, toLocalTimeStr } from '../lib/datetime';
import { DeleteConfirm, Field, FieldGroup, FormError, Sheet } from './ui';

type Preset = 'in15m' | 'in1h' | 'tonight9pm' | 'tomorrow9am';

const PRESETS: Array<{ id: Preset; label: string }> = [
  { id: 'in15m', label: 'In 15 min' },
  { id: 'in1h', label: 'In 1 hour' },
  { id: 'tonight9pm', label: 'Tonight, 9 PM' },
  { id: 'tomorrow9am', label: 'Tomorrow, 9 AM' }
];

function presetTime(id: Preset): Date {
  const target = new Date();
  if (id === 'in15m') target.setTime(Date.now() + 15 * 60000);
  if (id === 'in1h') target.setTime(Date.now() + 60 * 60000);
  if (id === 'tonight9pm') {
    target.setHours(21, 0, 0, 0);
    if (target.getTime() <= Date.now()) target.setDate(target.getDate() + 1);
  }
  if (id === 'tomorrow9am') {
    target.setDate(target.getDate() + 1);
    target.setHours(9, 0, 0, 0);
  }
  return target;
}

export const ReminderModal: React.FC = () => {
  const { isReminderModalOpen, editingReminder, closeReminderModal, createReminder, modifyReminder, removeReminder } = useReminders();
  const { activeTasks } = useTasks();
  const { subjects } = useSubjects();

  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [date, setDate] = useState(() => toLocalDateStr(new Date()));
  const [time, setTime] = useState(() => toLocalTimeStr(new Date(Date.now() + 3600000)));
  const [taskId, setTaskId] = useState('');
  const [subject, setSubject] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    const r = editingReminder;
    const when = r?.id ? new Date(r.reminderAt) : new Date(Date.now() + 3600000);
    setTitle(r?.title || '');
    setMessage(r?.message || '');
    setDate(toLocalDateStr(when));
    setTime(toLocalTimeStr(when));
    setTaskId(r?.taskId || '');
    setSubject(r?.subject || '');
    setErrorMessage('');
  }, [editingReminder, isReminderModalOpen]);

  const isEditingExisting = Boolean(editingReminder && editingReminder.id);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Say what the reminder is for.');
      return;
    }
    const [year, month, day] = date.split('-').map(Number);
    const [hours, minutes] = time.split(':').map(Number);
    if ([year, month, day, hours, minutes].some(n => Number.isNaN(n))) {
      setErrorMessage('Pick a valid date and time.');
      return;
    }
    const reminderAt = new Date(year, month - 1, day, hours, minutes, 0, 0).getTime();

    // Alerts only fire for reminders that come due while the app is open, so a
    // time already in the past would be saved and then silently never ring.
    const isUnchangedExistingTime = isEditingExisting && editingReminder?.reminderAt === reminderAt;
    if (reminderAt < Date.now() - 60000 && !isUnchangedExistingTime) {
      setErrorMessage('That time has already passed. Pick a time in the future.');
      return;
    }

    setIsSubmitting(true);
    try {
      const fields = {
        title: title.trim(),
        message: message.trim() || undefined,
        reminderAt,
        taskId: taskId || undefined,
        subject: subject || undefined,
        dismissed: false
      };
      if (isEditingExisting && editingReminder) await modifyReminder({ ...editingReminder, ...fields });
      else await createReminder({ ...fields, completed: false });
      closeReminderModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Could not save the reminder. Try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingReminder?.id) return;
    setIsSubmitting(true);
    try {
      await removeReminder(editingReminder.id);
      closeReminderModal();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Sheet
      open={isReminderModalOpen}
      title={isEditingExisting ? 'Edit reminder' : 'New reminder'}
      onClose={closeReminderModal}
      onSubmit={handleSubmit}
      footer={(
        <>
          <button type="submit" className="btn btn-primary btn-lg" disabled={isSubmitting}>
            {isEditingExisting ? 'Save changes' : 'Set reminder'}
          </button>
          {isEditingExisting && (
            <DeleteConfirm label="Delete reminder" confirmText="Delete this reminder?" onConfirm={handleDelete} disabled={isSubmitting} />
          )}
        </>
      )}
    >
      <FormError message={errorMessage} />

      <Field label="Remind me to">
        <input
          className="input"
          autoFocus={!isEditingExisting}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Submit the lab PDF on the portal"
        />
      </Field>

      <FieldGroup label="When">
        <div className="row" style={{ gap: 8, flexWrap: 'wrap' }}>
          {PRESETS.map(p => {
            const t = presetTime(p.id);
            const isOn = date === toLocalDateStr(t) && time === toLocalTimeStr(t);
            return (
              <button
                key={p.id}
                type="button"
                className="chip"
                aria-pressed={isOn}
                onClick={() => {
                  setDate(toLocalDateStr(t));
                  setTime(toLocalTimeStr(t));
                }}
              >
                {p.label}
              </button>
            );
          })}
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 8 }}>
          <input className="input" type="date" aria-label="Date" value={date} onChange={(e) => setDate(e.target.value)} />
          <input className="input" type="time" aria-label="Time" value={time} onChange={(e) => setTime(e.target.value)} />
        </div>
      </FieldGroup>

      <Field label="Details">
        <input className="input" value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Portal closes at 11:59 PM" />
      </Field>

      <div className="form-grid-2">
        <Field label="Subject">
          <select className="input" value={subject} onChange={(e) => setSubject(e.target.value)}>
            <option value="">None</option>
            {subjects.map(s => <option key={s.id} value={s.name}>{s.code ? `${s.name} (${s.code})` : s.name}</option>)}
          </select>
        </Field>
        <Field label="Linked task">
          <select className="input" value={taskId} onChange={(e) => setTaskId(e.target.value)} disabled={activeTasks.length === 0}>
            <option value="">None</option>
            {activeTasks.map(t => <option key={t.id} value={t.id}>{t.title}</option>)}
          </select>
        </Field>
      </div>
    </Sheet>
  );
};
