import React, { useEffect, useRef, useState } from 'react';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { DeleteConfirm, Field, FormError, Sheet } from './ui';
import { SubjectPicker } from './SubjectPicker';

export const NoteModal: React.FC = () => {
  const { isNoteModalOpen, editingNote, closeNoteModal, createNote, modifyNote, removeNote } = useNotes();
  const { subjects, getSubjectColor, createSubject } = useSubjects();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [subject, setSubject] = useState('General');
  const [customSubject, setCustomSubject] = useState('');
  const [isCustomSubject, setIsCustomSubject] = useState(false);
  const [pinned, setPinned] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const wasOpenRef = useRef(false);

  // Seeded once per opening; see TaskModal for why `subjects` must not re-seed.
  useEffect(() => {
    if (!isNoteModalOpen) {
      wasOpenRef.current = false;
      return;
    }
    if (wasOpenRef.current) return;
    wasOpenRef.current = true;

    const names = subjects.map(s => s.name);
    if (editingNote && editingNote.id) {
      setTitle(editingNote.title);
      setContent(editingNote.content);
      const known = names.includes(editingNote.subject) || editingNote.subject === 'General';
      setSubject(known ? editingNote.subject : 'General');
      setIsCustomSubject(!known);
      setCustomSubject(known ? '' : editingNote.subject);
      setPinned(editingNote.pinned || false);
    } else {
      setTitle('');
      setContent('');
      setSubject(editingNote?.subject || names[0] || 'General');
      setIsCustomSubject(false);
      setCustomSubject('');
      setPinned(false);
    }
    setErrorMessage('');
  }, [editingNote, isNoteModalOpen, subjects]);

  const isEditingExisting = Boolean(editingNote && editingNote.id);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Give the note a title.');
      return;
    }
    if (!content.trim()) {
      setErrorMessage('The note is empty.');
      return;
    }

    const finalSubject = isCustomSubject ? (customSubject.trim() || 'General') : subject;
    setIsSubmitting(true);
    try {
      const exists = subjects.some(s => s.name.toLowerCase() === finalSubject.toLowerCase());
      if (!exists && finalSubject.toLowerCase() !== 'general') {
        try {
          await createSubject({ name: finalSubject, color: getSubjectColor(finalSubject) });
        } catch (subErr) {
          console.warn('Failed to auto-create subject:', subErr);
        }
      }

      const fields = { title: title.trim(), content: content.trim(), subject: finalSubject, pinned };
      if (isEditingExisting && editingNote) await modifyNote({ ...editingNote, ...fields });
      else await createNote(fields);
      closeNoteModal();
    } catch (err) {
      console.error(err);
      setErrorMessage('Could not save the note. Try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingNote?.id) return;
    setIsSubmitting(true);
    try {
      await removeNote(editingNote.id);
      closeNoteModal();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Sheet
      open={isNoteModalOpen}
      title={isEditingExisting ? 'Edit note' : 'New note'}
      onClose={closeNoteModal}
      onSubmit={handleSubmit}
      footer={(
        <>
          <button type="submit" className="btn btn-primary btn-lg" disabled={isSubmitting}>
            {isEditingExisting ? 'Save changes' : 'Save note'}
          </button>
          {isEditingExisting && (
            <DeleteConfirm label="Delete note" confirmText="Delete this note? This cannot be undone." onConfirm={handleDelete} disabled={isSubmitting} />
          )}
        </>
      )}
    >
      <FormError message={errorMessage} />

      <Field label="Title">
        <input
          className="input"
          autoFocus={!isEditingExisting}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Lecture 8, backpropagation"
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

      <Field label="Note">
        <textarea
          className="input"
          rows={7}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Formulas, what will be on the exam, what the professor stressed"
          style={{ fontFamily: 'var(--font-sans)' }}
        />
      </Field>

      <div className="row" style={{ gap: 12 }}>
        <span className="grow">
          <span id="pin-label" style={{ display: 'block', fontSize: 14.5, fontWeight: 550 }}>Pin to Today</span>
          <span className="meta">Pinned notes show on the Today screen.</span>
        </span>
        <button type="button" role="switch" aria-checked={pinned} aria-labelledby="pin-label" className="switch" onClick={() => setPinned(p => !p)} />
      </div>
    </Sheet>
  );
};
