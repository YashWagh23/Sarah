import React, { useState, useEffect } from 'react';
import { 
  X, 
  Trash2, 
  Pin, 
  Check, 
  AlertCircle 
} from 'lucide-react';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';

export const NoteModal: React.FC = () => {
  const { isNoteModalOpen, editingNote, closeNoteModal, createNote, modifyNote, removeNote } = useNotes();
  const { subjects, getSubjectColor } = useSubjects();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [subject, setSubject] = useState('General');
  const [customSubject, setCustomSubject] = useState('');
  const [isCustomSubject, setIsCustomSubject] = useState(false);
  const [pinned, setPinned] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    const subjectNames = subjects.map(s => s.name);
    const defaultSub = subjectNames.length > 0 ? subjectNames[0] : 'General';

    if (editingNote && editingNote.id) {
      setTitle(editingNote.title);
      setContent(editingNote.content);
      if (subjectNames.includes(editingNote.subject)) {
        setSubject(editingNote.subject);
        setIsCustomSubject(false);
      } else {
        setSubject('Custom');
        setCustomSubject(editingNote.subject);
        setIsCustomSubject(true);
      }
      setPinned(editingNote.pinned || false);
      setShowDeleteConfirm(false);
    } else {
      // New note
      setTitle('');
      setContent('');
      setSubject(editingNote?.subject || defaultSub);
      setIsCustomSubject(false);
      setCustomSubject('');
      setPinned(false);
      setShowDeleteConfirm(false);
    }
    setErrorMessage('');
  }, [editingNote, isNoteModalOpen, subjects]);

  useEffect(() => {
    if (isNoteModalOpen) {
      const original = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      const handleKeyDown = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          closeNoteModal();
        }
      };
      window.addEventListener('keydown', handleKeyDown);
      return () => {
        document.body.style.overflow = original;
        window.removeEventListener('keydown', handleKeyDown);
      };
    }
  }, [isNoteModalOpen, closeNoteModal]);

  if (!isNoteModalOpen) return null;

  const isEditingExisting = Boolean(editingNote && editingNote.id);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Please enter a note title.');
      return;
    }
    if (!content.trim()) {
      setErrorMessage('Please enter note content.');
      return;
    }

    const finalSubject = isCustomSubject ? (customSubject.trim() || 'General') : subject;
    setIsSubmitting(true);

    try {
      if (isEditingExisting && editingNote) {
        await modifyNote({
          ...editingNote,
          title: title.trim(),
          content: content.trim(),
          subject: finalSubject,
          pinned
        });
      } else {
        await createNote({
          title: title.trim(),
          content: content.trim(),
          subject: finalSubject,
          pinned
        });
      }
      closeNoteModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to save academic note. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingNote || !editingNote.id) return;
    setIsSubmitting(true);
    try {
      await removeNote(editingNote.id);
      closeNoteModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Failed to delete note.');
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
      onClick={closeNoteModal}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label={isEditingExisting ? 'Edit Academic Note' : 'Capture Classroom Note'}
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
            {isEditingExisting ? 'Edit Academic Note' : 'Capture Classroom Note'}
          </h2>
          <button
            type="button"
            aria-label="Close note modal"
            onClick={closeNoteModal}
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
              Note Title *
            </label>
            <input
              type="text"
              autoFocus
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. ML Lecture 8 — Backpropagation equations"
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
              {/* If subjects is empty, render default General pill */}
              {subjects.length === 0 && (
                <button
                  type="button"
                  onClick={() => {
                    setSubject('General');
                    setIsCustomSubject(false);
                  }}
                  className="btn-press"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '5px',
                    padding: '6px 12px',
                    borderRadius: '16px',
                    border: (!isCustomSubject && subject === 'General') ? `1.5px solid var(--sarah-primary)` : '1px solid var(--sarah-outline-variant)',
                    backgroundColor: (!isCustomSubject && subject === 'General') ? 'rgba(68, 80, 183, 0.08)' : 'var(--sarah-surface-container-lowest)',
                    color: (!isCustomSubject && subject === 'General') ? 'var(--sarah-primary)' : 'var(--sarah-on-surface)',
                    fontSize: '12px',
                    fontWeight: (!isCustomSubject && subject === 'General') ? 600 : 500,
                    cursor: 'pointer'
                  }}
                >
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#6366F1' }} />
                  General
                </button>
              )}

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

              {/* Custom Subject Toggle Button */}
              <button
                type="button"
                onClick={() => {
                  setIsCustomSubject(true);
                  setSubject('Custom');
                }}
                className="btn-press"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '6px 12px',
                  borderRadius: '16px',
                  border: isCustomSubject ? `1.5px solid var(--sarah-primary)` : '1px dashed var(--sarah-outline)',
                  backgroundColor: isCustomSubject ? 'rgba(68, 80, 183, 0.08)' : 'transparent',
                  color: isCustomSubject ? 'var(--sarah-primary)' : 'var(--sarah-secondary)',
                  fontSize: '12px',
                  fontWeight: isCustomSubject ? 600 : 500,
                  cursor: 'pointer'
                }}
              >
                <span>+ Custom Subject</span>
              </button>
            </div>

            {/* Custom Subject Input Field */}
            {isCustomSubject && (
              <input
                type="text"
                value={customSubject}
                onChange={(e) => setCustomSubject(e.target.value)}
                placeholder="Enter subject name (e.g. Operating Systems)"
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '12px',
                  border: '1px solid var(--sarah-primary)',
                  fontSize: '13.5px',
                  outline: 'none',
                  backgroundColor: '#FFFFFF',
                  color: 'var(--sarah-on-background)',
                  marginTop: '4px'
                }}
              />
            )}
          </div>

          {/* 3. Note Content */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Note Content *
            </label>
            <textarea
              rows={5}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Jot down formulas, exam syllabus, professor hints, or important reminders..."
              style={{
                width: '100%',
                padding: '12px 14px',
                borderRadius: '14px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '14px',
                outline: 'none',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)',
                resize: 'none',
                fontFamily: 'inherit',
                lineHeight: 1.5
              }}
            />
          </div>

          {/* 4. Pin to Top iOS Switch */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '12px 14px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              borderRadius: '14px'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div
                style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '10px',
                  backgroundColor: pinned ? 'rgba(245, 158, 11, 0.15)' : 'var(--sarah-surface-container-high)',
                  color: pinned ? 'var(--sarah-tertiary)' : 'var(--sarah-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Pin size={16} fill={pinned ? 'currentColor' : 'none'} />
              </div>
              <div>
                <div style={{ fontSize: '13.5px', fontWeight: 600, color: 'var(--sarah-on-background)' }}>
                  Pin to Top
                </div>
                <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)' }}>
                  Keep prominent on Notes and Today screens
                </div>
              </div>
            </div>

            {/* iOS style toggle pill */}
            <button
              type="button"
              onClick={() => setPinned(prev => !prev)}
              style={{
                width: '46px',
                height: '26px',
                borderRadius: '13px',
                backgroundColor: pinned ? 'var(--sarah-tertiary)' : 'var(--sarah-outline)',
                border: 'none',
                position: 'relative',
                cursor: 'pointer',
                transition: 'background-color 0.2s ease',
                padding: 0
              }}
            >
              <span
                style={{
                  position: 'absolute',
                  top: '2px',
                  left: pinned ? '22px' : '2px',
                  width: '22px',
                  height: '22px',
                  borderRadius: '50%',
                  backgroundColor: '#FFFFFF',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.25)',
                  transition: 'left 0.2s cubic-bezier(0.16, 1, 0.3, 1)'
                }}
              />
            </button>
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
              <span>{isEditingExisting ? 'Save Changes' : 'Save Note'}</span>
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
                    <span>Delete Note</span>
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
                      Delete this note?
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
