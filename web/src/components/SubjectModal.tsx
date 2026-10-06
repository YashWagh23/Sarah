import React, { useEffect, useState } from 'react';
import { Check } from 'lucide-react';
import { DuplicateSubjectError, SUBJECT_PALETTE } from '../lib/db';
import { useSubjects } from '../context/SubjectsContext';
import { DeleteConfirm, Field, FieldGroup, FormError, Sheet } from './ui';

export const SubjectModal: React.FC = () => {
  const { isSubjectModalOpen, editingSubject, closeSubjectModal, createSubject, modifySubject, removeSubject } = useSubjects();

  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [faculty, setFaculty] = useState('');
  const [credits, setCredits] = useState('');
  const [weeklyHours, setWeeklyHours] = useState('');
  const [color, setColor] = useState(SUBJECT_PALETTE[0]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    const s = editingSubject;
    setName(s?.name ?? '');
    setCode(s?.code ?? '');
    setFaculty(s?.faculty ?? '');
    setCredits(s?.credits != null ? String(s.credits) : '');
    setWeeklyHours(s?.weeklyHours != null ? String(s.weeklyHours) : '');
    setColor(s?.color || SUBJECT_PALETTE[0]);
    setErrorMessage('');
  }, [editingSubject, isSubjectModalOpen]);

  const isEditingExisting = Boolean(editingSubject && editingSubject.id);

  const toOptionalNumber = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) return undefined;
    const parsed = Number(trimmed);
    return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage('Give the subject a name.');
      return;
    }
    setIsSubmitting(true);
    const details = {
      name: name.trim(),
      code: code.trim() || undefined,
      faculty: faculty.trim() || undefined,
      credits: toOptionalNumber(credits),
      weeklyHours: toOptionalNumber(weeklyHours),
      color
    };
    try {
      if (isEditingExisting && editingSubject) await modifySubject({ ...editingSubject, ...details });
      else await createSubject(details);
      closeSubjectModal();
    } catch (err) {
      console.error(err);
      setErrorMessage(err instanceof DuplicateSubjectError ? err.message : 'Could not save the subject. Try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!editingSubject?.id) return;
    setIsSubmitting(true);
    try {
      await removeSubject(editingSubject.id);
      closeSubjectModal();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Sheet
      open={isSubjectModalOpen}
      title={isEditingExisting ? 'Edit subject' : 'New subject'}
      onClose={closeSubjectModal}
      onSubmit={handleSubmit}
      footer={(
        <>
          <button type="submit" className="btn btn-primary btn-lg" disabled={isSubmitting}>
            {isEditingExisting ? 'Save changes' : 'Add subject'}
          </button>
          {isEditingExisting && (
            <DeleteConfirm
              label="Delete subject"
              confirmText="Delete this subject? Its tasks, notes and reminders are kept and move to General."
              onConfirm={handleDelete}
              disabled={isSubmitting}
            />
          )}
        </>
      )}
    >
      <FormError message={errorMessage} />

      <Field label="Name">
        <input className="input" autoFocus={!isEditingExisting} value={name} onChange={(e) => setName(e.target.value)} placeholder="Distributed Systems" />
      </Field>

      <div className="form-grid-2">
        <Field label="Course code">
          <input className="input" value={code} onChange={(e) => setCode(e.target.value)} placeholder="CS 405" />
        </Field>
        <Field label="Credits">
          <input className="input" type="number" inputMode="decimal" min={0} max={20} step={0.5} value={credits} onChange={(e) => setCredits(e.target.value)} placeholder="4" />
        </Field>
        <Field label="Faculty">
          <input className="input" value={faculty} onChange={(e) => setFaculty(e.target.value)} placeholder="Prof. Deshmukh" />
        </Field>
        <Field label="Hours a week">
          <input className="input" type="number" inputMode="decimal" min={0} max={40} step={0.5} value={weeklyHours} onChange={(e) => setWeeklyHours(e.target.value)} placeholder="5" />
        </Field>
      </div>

      <FieldGroup label="Colour">
        <div role="radiogroup" aria-label="Colour" className="row" style={{ gap: 10, flexWrap: 'wrap' }}>
          {SUBJECT_PALETTE.map(c => {
            const isSelected = color.toLowerCase() === c.toLowerCase();
            return (
              <button
                key={c}
                type="button"
                role="radio"
                aria-checked={isSelected}
                aria-label={`Colour ${c}`}
                onClick={() => setColor(c)}
                className="press"
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: 'var(--r-pill)',
                  background: c,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#FDFDFF',
                  boxShadow: isSelected ? `0 0 0 2px var(--surface), 0 0 0 4px ${c}` : 'none'
                }}
              >
                {isSelected && <Check size={17} strokeWidth={3} />}
              </button>
            );
          })}
        </div>
      </FieldGroup>
    </Sheet>
  );
};
