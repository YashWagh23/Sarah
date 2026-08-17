import React, { useState, useEffect } from 'react';
import { 
  X, 
  Trash2, 
  Flame, 
  BookOpen, 
  Check, 
  AlertCircle 
} from 'lucide-react';
import { type TaskPriority } from '../lib/db';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';

export const TaskModal: React.FC = () => {
  const { isTaskModalOpen, editingTask, closeTaskModal, createTask, modifyTask, removeTask } = useTasks();
  const { subjects, getSubjectColor } = useSubjects();

  const getTodayStr = () => {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  const getTomorrowStr = () => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  const getNextWeekStr = () => {
    const d = new Date();
    d.setDate(d.getDate() + 7);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  // Form State
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [subject, setSubject] = useState('General');
  const [customSubject, setCustomSubject] = useState('');
  const [isCustomSubject, setIsCustomSubject] = useState(false);
  const [deadline, setDeadline] = useState(getTodayStr());
  const [deadlineTime, setDeadlineTime] = useState('23:59');
  const [priority, setPriority] = useState<TaskPriority>('must');
  const [estimatedMinutes, setEstimatedMinutes] = useState(45);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    const subjectNames = subjects.map(s => s.name);
    const defaultSub = subjectNames.length > 0 ? subjectNames[0] : 'General';

    if (editingTask) {
      setTitle(editingTask.title);
      setDescription(editingTask.description || '');
      if (subjectNames.includes(editingTask.subject)) {
        setSubject(editingTask.subject);
        setIsCustomSubject(false);
      } else {
        setSubject('Custom');
        setCustomSubject(editingTask.subject);
        setIsCustomSubject(true);
      }
      setDeadline(editingTask.deadline || getTodayStr());
      setDeadlineTime(editingTask.deadlineTime || '');
      setPriority(editingTask.priority);
      setEstimatedMinutes(editingTask.estimatedMinutes);
      setShowDeleteConfirm(false);
    } else {
      // Reset for new task
      setTitle('');
      setDescription('');
      setSubject(defaultSub);
      setIsCustomSubject(false);
      setCustomSubject('');
      setDeadline(getTodayStr());
      setDeadlineTime('');
      setPriority('must');
      setEstimatedMinutes(45);
      setShowDeleteConfirm(false);
    }
    setErrorMessage('');
  }, [editingTask, isTaskModalOpen, subjects]);

  useEffect(() => {
    if (isTaskModalOpen) {
      const original = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      return () => {
        document.body.style.overflow = original;
      };
    }
  }, [isTaskModalOpen]);

  if (!isTaskModalOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Please enter a task title.');
      return;
    }

    const finalSubject = isCustomSubject ? (customSubject.trim() || 'General') : subject;
    setIsSubmitting(true);

    try {
      if (editingTask) {
        await modifyTask({
          ...editingTask,
          title: title.trim(),
          description: description.trim() || undefined,
          subject: finalSubject,
          deadline,
          deadlineTime: deadlineTime || undefined,
          priority,
          estimatedMinutes: Number(estimatedMinutes) || 30
        });
      } else {
        await createTask({
          title: title.trim(),
          description: description.trim() || undefined,
          subject: finalSubject,
          deadline,
          deadlineTime: deadlineTime || undefined,
          priority,
          estimatedMinutes: Number(estimatedMinutes) || 30,
          completed: false
        });
      }
      closeTaskModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to save task. Please try again.');
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
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to delete task.');
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
      onClick={closeTaskModal}
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
          <h2 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--sarah-on-background)', margin: 0 }}>
            {editingTask ? 'Edit Academic Task' : 'New Academic Task'}
          </h2>
          <button
            type="button"
            onClick={closeTaskModal}
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

        {/* Form Body Scroll Area */}
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

          {/* 1. Title Input */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Task Title *
            </label>
            <input
              type="text"
              autoFocus
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Complete ML Assignment 3"
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

          {/* 2. Subject Selector */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Subject
            </label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              {subjects.map((sub) => {
                const isSelected = !isCustomSubject && subject.toLowerCase() === sub.name.toLowerCase();
                const dotColor = sub.color || getSubjectColor(sub.name);
                return (
                  <button
                    key={sub.id}
                    type="button"
                    onClick={() => {
                      setSubject(sub.name);
                      setIsCustomSubject(false);
                    }}
                    className="btn-press"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '5px',
                      padding: '6px 12px',
                      borderRadius: '16px',
                      border: isSelected ? `1.5px solid var(--sarah-primary)` : '1px solid var(--sarah-outline-variant)',
                      backgroundColor: isSelected ? 'rgba(68, 80, 183, 0.08)' : 'var(--sarah-surface-container-lowest)',
                      color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                      fontSize: '12px',
                      fontWeight: isSelected ? 600 : 500,
                      cursor: 'pointer'
                    }}
                  >
                    <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: dotColor }} />
                    {sub.name}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 3. Priority Segmented Control */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Priority Group
            </label>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr 1fr',
                gap: '6px',
                backgroundColor: 'var(--sarah-surface-container-low)',
                padding: '4px',
                borderRadius: '14px'
              }}
            >
              <button
                type="button"
                onClick={() => setPriority('must')}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px 4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px',
                  backgroundColor: priority === 'must' ? '#FFFFFF' : 'transparent',
                  color: priority === 'must' ? 'var(--sarah-error)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: priority === 'must' ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12.5px',
                  fontWeight: priority === 'must' ? 700 : 500,
                  cursor: 'pointer'
                }}
              >
                <Flame size={14} color={priority === 'must' ? 'var(--sarah-error)' : 'currentColor'} />
                <span>Must Do</span>
              </button>

              <button
                type="button"
                onClick={() => setPriority('should')}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px 4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px',
                  backgroundColor: priority === 'should' ? '#FFFFFF' : 'transparent',
                  color: priority === 'should' ? 'var(--sarah-tertiary)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: priority === 'should' ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12.5px',
                  fontWeight: priority === 'should' ? 700 : 500,
                  cursor: 'pointer'
                }}
              >
                <BookOpen size={14} color={priority === 'should' ? 'var(--sarah-tertiary)' : 'currentColor'} />
                <span>Should Do</span>
              </button>

              <button
                type="button"
                onClick={() => setPriority('later')}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px 4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: priority === 'later' ? '#FFFFFF' : 'transparent',
                  color: priority === 'later' ? 'var(--sarah-on-background)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: priority === 'later' ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12.5px',
                  fontWeight: priority === 'later' ? 700 : 500,
                  cursor: 'pointer'
                }}
              >
                <span>Later</span>
              </button>
            </div>
          </div>

          {/* 4. Deadline Presets & Date Input */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Deadline
            </label>
            <div style={{ display: 'flex', gap: '6px' }}>
              <button
                type="button"
                onClick={() => setDeadline(getTodayStr())}
                className="btn-press"
                style={{
                  flex: 1,
                  padding: '7px 4px',
                  borderRadius: '10px',
                  border: deadline === getTodayStr() ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
                  backgroundColor: deadline === getTodayStr() ? 'rgba(68, 80, 183, 0.08)' : 'transparent',
                  color: deadline === getTodayStr() ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                Today
              </button>
              <button
                type="button"
                onClick={() => setDeadline(getTomorrowStr())}
                className="btn-press"
                style={{
                  flex: 1,
                  padding: '7px 4px',
                  borderRadius: '10px',
                  border: deadline === getTomorrowStr() ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
                  backgroundColor: deadline === getTomorrowStr() ? 'rgba(68, 80, 183, 0.08)' : 'transparent',
                  color: deadline === getTomorrowStr() ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                Tomorrow
              </button>
              <button
                type="button"
                onClick={() => setDeadline(getNextWeekStr())}
                className="btn-press"
                style={{
                  flex: 1,
                  padding: '7px 4px',
                  borderRadius: '10px',
                  border: deadline === getNextWeekStr() ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
                  backgroundColor: deadline === getNextWeekStr() ? 'rgba(68, 80, 183, 0.08)' : 'transparent',
                  color: deadline === getNextWeekStr() ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                  fontSize: '12px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                Next Week
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '8px' }}>
              <input
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
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
                value={deadlineTime}
                onChange={(e) => setDeadlineTime(e.target.value)}
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

          {/* 5. Estimated Time Duration */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
                Estimated Session
              </label>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-primary)' }}>
                {estimatedMinutes} mins
              </span>
            </div>
            <div style={{ display: 'flex', gap: '6px' }}>
              {[15, 30, 45, 60, 90].map((mins) => (
                <button
                  key={mins}
                  type="button"
                  onClick={() => setEstimatedMinutes(mins)}
                  className="btn-press"
                  style={{
                    flex: 1,
                    padding: '6px 2px',
                    borderRadius: '10px',
                    border: estimatedMinutes === mins ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
                    backgroundColor: estimatedMinutes === mins ? 'rgba(68, 80, 183, 0.08)' : 'transparent',
                    color: estimatedMinutes === mins ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer'
                  }}
                >
                  {mins}m
                </button>
              ))}
            </div>
          </div>

          {/* 6. Optional Description */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Description / Notes (Optional)
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Chapter 4 review, exercises 4.1 to 4.5"
              style={{
                width: '100%',
                padding: '10px 12px',
                borderRadius: '12px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '13.5px',
                outline: 'none',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)',
                resize: 'none',
                fontFamily: 'inherit'
              }}
            />
          </div>

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
              <span>{editingTask ? 'Save Changes' : 'Create Task'}</span>
            </button>

            {/* Delete button if editing */}
            {editingTask && (
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
                    <span>Delete Task</span>
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
                      Delete this task?
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
