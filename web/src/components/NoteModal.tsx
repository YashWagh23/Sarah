import React, { useState, useEffect } from 'react';
import { 
  X, 
  Trash2, 
  Pin, 
  Check, 
  AlertCircle 
} from 'lucide-react';
import { DEFAULT_SUBJECTS, SUBJECT_COLORS } from '../lib/db';
import { useNotes } from '../context/NotesContext';

export const NoteModal: React.FC = () => {
  const { isNoteModalOpen, editingNote, closeNoteModal, createNote, modifyNote, removeNote, allSubjects } = useNotes();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [subject, setSubject] = useState(DEFAULT_SUBJECTS[0]);
  const [customSubject, setCustomSubject] = useState('');
  const [isCustomSubject, setIsCustomSubject] = useState(false);
  const [pinned, setPinned] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (editingNote && editingNote.id) {
      setTitle(editingNote.title);
      setContent(editingNote.content);
      if (allSubjects.includes(editingNote.subject)) {
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
      setSubject(editingNote?.subject || DEFAULT_SUBJECTS[0]);
      setIsCustomSubject(false);
      setCustomSubject('');
      setPinned(false);
      setShowDeleteConfirm(false);
    }
    setErrorMessage('');
  }, [editingNote, isNoteModalOpen, allSubjects]);

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
      setErrorMessage('Failed to save note. Please try again.');
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
              placeholder="e.g. ML Lecture 8 — Backpropagation formulas"
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
              {allSubjects.map((sub) => {
                const isSelected = !isCustomSubject && subject === sub;
                const dotColor = SUBJECT_COLORS[sub] || '#6366F1';
                return (
                  <button
                    key={sub}
                    type="button"
                    onClick={() => {
                      setSubject(sub);
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
                    {sub}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 3. Note Content */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', color: 'var(--sarah-on-surface-variant)' }}>
              Content / Instructions *
            </label>
            <textarea
              rows={5}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Type lecture concepts, lab guidelines, professor instructions, or exam pointers..."
              style={{
                width: '100%',
                padding: '12px 14px',
                borderRadius: '14px',
                border: '1px solid var(--sarah-outline-variant)',
                fontSize: '14px',
                lineHeight: 1.45,
                outline: 'none',
                backgroundColor: 'var(--sarah-surface-container-low)',
                color: 'var(--sarah-on-background)',
                resize: 'none',
                fontFamily: 'inherit'
              }}
            />
          </div>

          {/* 4. Pinned to Top Toggle */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '12px 16px',
              borderRadius: '14px',
              backgroundColor: pinned ? 'rgba(245, 158, 11, 0.08)' : 'var(--sarah-surface-container-low)',
              border: pinned ? '1px solid rgba(245, 158, 11, 0.25)' : '1px solid var(--sarah-outline-variant)',
              transition: 'all 0.15s ease'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div
                style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '10px',
                  backgroundColor: pinned ? 'rgba(245, 158, 11, 0.15)' : '#FFFFFF',
                  color: pinned ? 'var(--sarah-tertiary)' : 'var(--sarah-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Pin size={16} fill={pinned ? 'currentColor' : 'none'} style={{ transform: pinned ? 'rotate(45deg)' : 'none' }} />
              </div>
              <div>
                <div style={{ fontSize: '13.5px', fontWeight: 600, color: 'var(--sarah-on-background)' }}>
                  Pin to Top
                </div>
                <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)' }}>
                  Keep visible at the top of Notes & Today screen
                </div>
              </div>
            </div>

            {/* iOS Switch Toggle */}
            <button
              type="button"
              onClick={() => setPinned(prev => !prev)}
              style={{
                width: '44px',
                height: '26px',
                borderRadius: '13px',
                backgroundColor: pinned ? '#F59E0B' : 'var(--sarah-outline-variant)',
                border: 'none',
                padding: '2px',
                display: 'flex',
                alignItems: 'center',
                cursor: 'pointer',
                transition: 'background-color 0.2s ease',
                position: 'relative'
              }}
            >
              <div
                style={{
                  width: '22px',
                  height: '22px',
                  borderRadius: '50%',
                  backgroundColor: '#FFFFFF',
                  boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
                  transform: pinned ? 'translateX(18px)' : 'translateX(0)',
                  transition: 'transform 0.2s ease'
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
