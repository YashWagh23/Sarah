import React, { useState, useEffect } from 'react';
import { 
  X, 
  Trash2, 
  Bell, 
  Check, 
  AlertCircle,
  Link as LinkIcon,
  BookOpen
} from 'lucide-react';
import { useReminders } from '../context/RemindersContext';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';

export const ReminderModal: React.FC = () => {
  const { isReminderModalOpen, editingReminder, closeReminderModal, createReminder, modifyReminder, removeReminder } = useReminders();
  const { activeTasks } = useTasks();
  const { subjects } = useSubjects();

  const getLocalDateStr = (d: Date) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  const getLocalTimeStr = (d: Date) => {
    const hours = String(d.getHours()).padStart(2, '0');
    const mins = String(d.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
  };

  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [date, setDate] = useState(() => getLocalDateStr(new Date()));
  const [time, setTime] = useState(() => {
    const inOneHour = new Date(Date.now() + 3600000);
    return getLocalTimeStr(inOneHour);
  });
  const [selectedTaskId, setSelectedTaskId] = useState<string>('');
  const [selectedSubject, setSelectedSubject] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (editingReminder && editingReminder.id) {
      setTitle(editingReminder.title);
      setMessage(editingReminder.message || '');
      const d = new Date(editingReminder.reminderAt);
      setDate(getLocalDateStr(d));
      setTime(getLocalTimeStr(d));
      setSelectedTaskId(editingReminder.taskId || '');
      setSelectedSubject(editingReminder.subject || '');
      setShowDeleteConfirm(false);
    } else {
      // New reminder
      const inOneHour = new Date(Date.now() + 3600000);
      setTitle(editingReminder?.title || '');
      setMessage('');
      setDate(getLocalDateStr(inOneHour));
      setTime(getLocalTimeStr(inOneHour));
      setSelectedTaskId(editingReminder?.taskId || '');
      setSelectedSubject(editingReminder?.subject || '');
      setShowDeleteConfirm(false);
    }
    setErrorMessage('');
  }, [editingReminder, isReminderModalOpen]);

  if (!isReminderModalOpen) return null;

  const isEditingExisting = Boolean(editingReminder && editingReminder.id);

  // Quick Time Presets
  const handleQuickPreset = (type: 'in15m' | 'in1h' | 'tonight9pm' | 'tomorrow9am') => {
    const target = new Date();
    if (type === 'in15m') {
      target.setTime(Date.now() + 15 * 60000);
    } else if (type === 'in1h') {
      target.setTime(Date.now() + 60 * 60000);
    } else if (type === 'tonight9pm') {
      target.setHours(21, 0, 0, 0);
      if (target.getTime() <= Date.now()) {
        target.setDate(target.getDate() + 1);
      }
    } else if (type === 'tomorrow9am') {
      target.setDate(target.getDate() + 1);
      target.setHours(9, 0, 0, 0);
    }
    setDate(getLocalDateStr(target));
    setTime(getLocalTimeStr(target));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Please enter a reminder title.');
      return;
    }

    const [year, month, day] = date.split('-').map(Number);
    const [hours, minutes] = time.split(':').map(Number);

    if (isNaN(year) || isNaN(month) || isNaN(day) || isNaN(hours) || isNaN(minutes)) {
      setErrorMessage('Please select a valid date and time.');
      return;
    }

    const reminderDate = new Date(year, month - 1, day, hours, minutes, 0, 0);
    const reminderAt = reminderDate.getTime();

    setIsSubmitting(true);

    try {
      if (isEditingExisting && editingReminder) {
        await modifyReminder({
          ...editingReminder,
          title: title.trim(),
          message: message.trim() || undefined,
          reminderAt,
          taskId: selectedTaskId || undefined,
          subject: selectedSubject || undefined,
          dismissed: false
        });
      } else {
        await createReminder({
          title: title.trim(),
          message: message.trim() || undefined,
          reminderAt,
          taskId: selectedTaskId || undefined,
          subject: selectedSubject || undefined,
          completed: false,
          dismissed: false
        });
      }
      closeReminderModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to save reminder. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingReminder || !editingReminder.id) return;
    setIsSubmitting(true);
    try {
      await removeReminder(editingReminder.id);
      closeReminderModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to delete reminder.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.45)',
        backdropFilter: 'blur(6px)',
        WebkitBackdropFilter: 'blur(6px)',
        zIndex: 100,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
      }}
      onClick={closeReminderModal}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="glass-card"
        style={{
          width: '100%',
          maxWidth: '540px',
          maxHeight: '90dvh',
          backgroundColor: '#FFFFFF',
          borderTopLeftRadius: '28px',
          borderTopRightRadius: '28px',
          borderBottomLeftRadius: 0,
          borderBottomRightRadius: 0,
          boxShadow: '0 -10px 40px rgba(0, 0, 0, 0.15)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          animation: 'slideUp 0.26s cubic-bezier(0.16, 1, 0.3, 1) forwards'
        }}
      >
        {/* iOS Drag Handle */}
        <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '10px', paddingBottom: '4px' }}>
          <div style={{ width: '36px', height: '4px', borderRadius: '2px', backgroundColor: 'var(--sarah-surface-container-high)' }} />
        </div>

        {/* Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 20px',
            borderBottom: '1px solid var(--sarah-outline-variant)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Bell size={18} color="var(--sarah-primary)" />
            <h2 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--sarah-on-background)', margin: 0 }}>
              {isEditingExisting ? 'Edit Reminder' : 'Set Academic Reminder'}
            </h2>
          </div>
          <button
            type="button"
            onClick={closeReminderModal}
            style={{
              background: 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '50%',
              width: '30px',
              height: '30px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: 'var(--sarah-secondary)'
            }}
          >
            <X size={17} />
          </button>
        </div>

        {/* Form Body */}
        <form
          onSubmit={handleSubmit}
          className="scroll-container"
          style={{
            padding: '18px 20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '18px',
            overflowY: 'auto'
          }}
        >
          {errorMessage && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '10px 14px',
                borderRadius: '12px',
                backgroundColor: 'var(--sarah-error-container)',
                color: 'var(--sarah-on-error-container)',
                fontSize: '12.5px',
                fontWeight: 500
              }}
            >
              <AlertCircle size={16} />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* 1. Title */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Reminder Title *
            </label>
            <input
              type="text"
              autoFocus
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Submit Lab Assignment PDF"
              style={{
                width: '100%',
                padding: '12px 14px',
                borderRadius: '14px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '15px',
                outline: 'none',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)',
                fontWeight: 500
              }}
            />
          </div>

          {/* 2. Optional Message */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Message / Notes (Optional)
            </label>
            <input
              type="text"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="e.g. Check submission portal before 11:59 PM"
              style={{
                width: '100%',
                padding: '10px 12px',
                borderRadius: '12px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '13.5px',
                outline: 'none',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)'
              }}
            />
          </div>

          {/* 3. Quick Presets */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Quick Presets
            </label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              <button
                type="button"
                onClick={() => handleQuickPreset('in15m')}
                className="btn-press"
                style={{
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '12px',
                  padding: '6px 12px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 500,
                  cursor: 'pointer'
                }}
              >
                In 15m
              </button>
              <button
                type="button"
                onClick={() => handleQuickPreset('in1h')}
                className="btn-press"
                style={{
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '12px',
                  padding: '6px 12px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 500,
                  cursor: 'pointer'
                }}
              >
                In 1 hour
              </button>
              <button
                type="button"
                onClick={() => handleQuickPreset('tonight9pm')}
                className="btn-press"
                style={{
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '12px',
                  padding: '6px 12px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 500,
                  cursor: 'pointer'
                }}
              >
                Tonight 9 PM
              </button>
              <button
                type="button"
                onClick={() => handleQuickPreset('tomorrow9am')}
                className="btn-press"
                style={{
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '12px',
                  padding: '6px 12px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 500,
                  cursor: 'pointer'
                }}
              >
                Tomorrow 9 AM
              </button>
            </div>
          </div>

          {/* 4. Date & Time Inputs */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Reminder Time *
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '8px' }}>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '12px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13.5px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-background)',
                  outline: 'none'
                }}
              />
              <input
                type="time"
                value={time}
                onChange={(e) => setTime(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '12px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13.5px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-background)',
                  outline: 'none'
                }}
              />
            </div>
          </div>

          {/* 5. Subject Selector (Optional) */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <BookOpen size={12} color="var(--sarah-secondary)" />
              <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
                Subject (Optional)
              </label>
            </div>
            <select
              value={selectedSubject}
              onChange={(e) => setSelectedSubject(e.target.value)}
              style={{
                width: '100%',
                padding: '10px 12px',
                borderRadius: '12px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '13.5px',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)',
                outline: 'none'
              }}
            >
              <option value="">None (No specific subject)</option>
              {subjects.map(s => (
                <option key={s.id} value={s.name}>
                  {s.name} {s.code ? `(${s.code})` : ''}
                </option>
              ))}
            </select>
          </div>

          {/* 6. Link to Task (Optional) */}
          {activeTasks.length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <LinkIcon size={12} color="var(--sarah-secondary)" />
                <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
                  Link to Task (Optional)
                </label>
              </div>
              <select
                value={selectedTaskId}
                onChange={(e) => setSelectedTaskId(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '12px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13.5px',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-on-background)',
                  outline: 'none'
                }}
              >
                <option value="">None (Independent reminder)</option>
                {activeTasks.map(t => (
                  <option key={t.id} value={t.id}>
                    {t.title} ({t.subject})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Actions Bar */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', paddingTop: '6px' }} className="safe-bottom">
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-press"
              style={{
                width: '100%',
                padding: '14px',
                borderRadius: '14px',
                backgroundColor: 'var(--sarah-primary)',
                color: '#FFFFFF',
                border: 'none',
                fontSize: '15px',
                fontWeight: 700,
                cursor: 'pointer',
                boxShadow: '0 4px 14px rgba(68, 80, 183, 0.35)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px'
              }}
            >
              <Check size={18} strokeWidth={2.5} />
              <span>{isEditingExisting ? 'Save Changes' : 'Set Reminder'}</span>
            </button>

            {/* Delete button if editing */}
            {isEditingExisting && (
              <>
                {!showDeleteConfirm ? (
                  <button
                    type="button"
                    onClick={() => setShowDeleteConfirm(true)}
                    className="btn-press"
                    style={{
                      width: '100%',
                      padding: '10px',
                      borderRadius: '12px',
                      backgroundColor: 'transparent',
                      color: 'var(--sarah-error)',
                      border: '1px solid var(--sarah-error-container)',
                      fontSize: '13px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '6px'
                    }}
                  >
                    <Trash2 size={15} />
                    <span>Delete Reminder</span>
                  </button>
                ) : (
                  <div
                    style={{
                      display: 'flex',
                      gap: '8px',
                      backgroundColor: 'var(--sarah-error-container)',
                      padding: '8px 12px',
                      borderRadius: '12px',
                      alignItems: 'center',
                      justifyContent: 'space-between'
                    }}
                  >
                    <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--sarah-on-error-container)' }}>
                      Are you sure?
                    </span>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <button
                        type="button"
                        onClick={() => setShowDeleteConfirm(false)}
                        style={{
                          background: '#FFFFFF',
                          border: 'none',
                          padding: '5px 10px',
                          borderRadius: '8px',
                          fontSize: '11.5px',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        Cancel
                      </button>
                      <button
                        type="button"
                        onClick={handleDelete}
                        style={{
                          background: 'var(--sarah-error)',
                          color: '#FFFFFF',
                          border: 'none',
                          padding: '5px 12px',
                          borderRadius: '8px',
                          fontSize: '11.5px',
                          fontWeight: 700,
                          cursor: 'pointer'
                        }}
                      >
                        Yes, Delete
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};
