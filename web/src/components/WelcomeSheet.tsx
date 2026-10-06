import React, { useState } from 'react';
import { useUserProfile } from '../context/UserProfileContext';
import { DEFAULT_USER_PROFILE } from '../lib/db';
import { CollegeDaysPicker } from './CollegeDaysPicker';
import { Field, FieldGroup, Sheet } from './ui';

/**
 * Shown once on a fresh install. Sarah's plan depends on when the student's day
 * frees up and when they go to sleep, so those are asked up front.
 */
export const WelcomeSheet: React.FC = () => {
  const { isFirstRun, isLoading, updateProfile } = useUserProfile();
  const [name, setName] = useState('');
  const [targetBedtime, setTargetBedtime] = useState(DEFAULT_USER_PROFILE.targetBedtime);
  const [collegeEndTime, setCollegeEndTime] = useState(DEFAULT_USER_PROFILE.collegeEndTime);
  const [commuteMinutes, setCommuteMinutes] = useState(String(DEFAULT_USER_PROFILE.commuteMinutes));
  const [collegeDays, setCollegeDays] = useState<number[]>(DEFAULT_USER_PROFILE.collegeDays);
  const [isSaving, setIsSaving] = useState(false);

  const save = async (skip: boolean) => {
    setIsSaving(true);
    try {
      const first = name.trim().split(' ')[0];
      await updateProfile(
        skip
          ? {}
          : {
              name: name.trim(),
              targetBedtime,
              collegeEndTime,
              commuteMinutes: Math.min(180, Math.max(0, Number(commuteMinutes) || 0)),
              collegeDays
            },
        skip ? null : `Welcome${first ? `, ${first}` : ''}. Your evenings are planned around this.`
      );
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Sheet
      open={!isLoading && isFirstRun}
      title="Welcome to Sarah"
      dismissible={false}
      onClose={() => save(true)}
      onSubmit={(e) => {
        e.preventDefault();
        save(false);
      }}
      footer={(
        <>
          <button type="submit" className="btn btn-primary btn-lg" disabled={isSaving}>Plan my evenings</button>
          <button type="button" className="btn btn-ghost" style={{ color: 'var(--text-2)' }} disabled={isSaving} onClick={() => save(true)}>
            Skip for now
          </button>
        </>
      )}
    >
      <p style={{ fontSize: 15, color: 'var(--text-2)', lineHeight: 1.5, marginTop: -8 }}>
        Each evening Sarah fits your deadlines into the time between classes and bedtime, and tells you what to do next.
        Everything stays on this device.
      </p>

      <Field label="First name">
        <input className="input" autoFocus autoComplete="given-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Priya" />
      </Field>

      <div className="form-grid-2">
        <Field label="Classes end">
          <input className="input" type="time" value={collegeEndTime} onChange={(e) => setCollegeEndTime(e.target.value)} />
        </Field>
        <Field label="Commute (min)">
          <input className="input" type="number" inputMode="numeric" min={0} max={180} value={commuteMinutes} onChange={(e) => setCommuteMinutes(e.target.value)} />
        </Field>
      </div>

      <Field label="Bedtime you want to keep">
        <input className="input" type="time" value={targetBedtime} onChange={(e) => setTargetBedtime(e.target.value)} />
      </Field>

      <FieldGroup label="Days with classes" hint="You can change all of this later in Profile.">
        <CollegeDaysPicker value={collegeDays} onChange={setCollegeDays} />
      </FieldGroup>
    </Sheet>
  );
};
