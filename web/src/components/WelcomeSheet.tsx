import React, { useState } from 'react';
import { Sparkles, Check } from 'lucide-react';
import { useUserProfile } from '../context/UserProfileContext';
import { DEFAULT_USER_PROFILE } from '../lib/db';
import { CollegeDaysPicker } from './CollegeDaysPicker';

const fieldStyle: React.CSSProperties = {
  width: '100%',
  padding: '10px 12px',
  borderRadius: '12px',
  border: '1px solid var(--sarah-outline-variant)',
  fontSize: '14px',
  outline: 'none',
  backgroundColor: 'var(--sarah-surface-container-low)',
  color: 'var(--sarah-on-background)'
};

const labelStyle: React.CSSProperties = {
  fontSize: '11.5px',
  fontWeight: 700,
  letterSpacing: '0.04em',
  textTransform: 'uppercase',
  color: 'var(--sarah-on-surface-variant)'
};

/**
 * Shown once on a fresh install. Sarah's plan depends on when the student's day
 * actually frees up and when they go to sleep, so those are asked up front.
 */
export const WelcomeSheet: React.FC = () => {
  const { isFirstRun, isLoading, updateProfile } = useUserProfile();
  const [name, setName] = useState('');
  const [targetBedtime, setTargetBedtime] = useState(DEFAULT_USER_PROFILE.targetBedtime);
  const [collegeEndTime, setCollegeEndTime] = useState(DEFAULT_USER_PROFILE.collegeEndTime);
  const [commuteMinutes, setCommuteMinutes] = useState(DEFAULT_USER_PROFILE.commuteMinutes);
  const [collegeDays, setCollegeDays] = useState<number[]>(DEFAULT_USER_PROFILE.collegeDays);
  const [isSaving, setIsSaving] = useState(false);

  if (isLoading || !isFirstRun) return null;

  const save = async (skip: boolean) => {
    setIsSaving(true);
    try {
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
        skip ? null : `Welcome${name.trim() ? `, ${name.trim().split(' ')[0]}` : ''}! Your evening is planned around this.`
      );
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'var(--sarah-scrim)',
        backdropFilter: 'blur(6px)',
        WebkitBackdropFilter: 'blur(6px)',
        zIndex: 110,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center'
      }}
    >
      <form
        role="dialog"
        aria-modal="true"
        aria-labelledby="welcome-title"
        onSubmit={(e) => {
          e.preventDefault();
          save(false);
        }}
        className="glass-card scroll-container safe-bottom"
        style={{
          width: '100%',
          maxWidth: '540px',
          maxHeight: '92dvh',
          overflowY: 'auto',
          backgroundColor: 'var(--sarah-elevated)',
          borderRadius: '28px 28px 0 0',
          boxShadow: 'var(--sheet-shadow)',
          padding: '22px 20px 20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
          animation: 'slideUp 0.28s cubic-bezier(0.16, 1, 0.3, 1) forwards'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--sarah-primary)' }}>
            <Sparkles size={18} />
            <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase' }}>
              Welcome to Sarah
            </span>
          </div>
          <h2 id="welcome-title" style={{ fontSize: '21px', fontWeight: 800, color: 'var(--sarah-on-background)', letterSpacing: '-0.02em', margin: 0 }}>
            Every evening, Sarah tells you what to do next.
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-on-surface-variant)', margin: 0, lineHeight: 1.5 }}>
            It fits your deadlines into the time you really have between classes and bedtime. Everything stays on this device.
          </p>
        </div>

        <label style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={labelStyle}>Your first name</span>
          <input
            type="text"
            autoFocus
            autoComplete="given-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="What should Sarah call you?"
            style={fieldStyle}
          />
        </label>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
          <label style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <span style={labelStyle}>Classes end</span>
            <input type="time" value={collegeEndTime} onChange={(e) => setCollegeEndTime(e.target.value)} style={fieldStyle} />
          </label>
          <label style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <span style={labelStyle}>Commute (min)</span>
            <input
              type="number"
              inputMode="numeric"
              min={0}
              max={180}
              value={commuteMinutes}
              onChange={(e) => setCommuteMinutes(Number(e.target.value))}
              style={fieldStyle}
            />
          </label>
          <label style={{ display: 'flex', flexDirection: 'column', gap: '6px', gridColumn: '1 / -1' }}>
            <span style={labelStyle}>Bedtime you want to keep</span>
            <input type="time" value={targetBedtime} onChange={(e) => setTargetBedtime(e.target.value)} style={fieldStyle} />
          </label>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={labelStyle}>Days with classes</span>
          <CollegeDaysPicker value={collegeDays} onChange={setCollegeDays} />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', paddingTop: '4px' }}>
          <button
            type="submit"
            disabled={isSaving}
            className="btn-press"
            style={{
              width: '100%',
              padding: '14px',
              borderRadius: '14px',
              backgroundColor: 'var(--sarah-primary)',
              color: 'var(--sarah-on-primary)',
              border: 'none',
              fontSize: '15px',
              fontWeight: 700,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              boxShadow: '0 4px 14px rgba(var(--sarah-primary-rgb), 0.35)'
            }}
          >
            <Check size={18} strokeWidth={2.5} />
            <span>Plan my evenings</span>
          </button>
          <button
            type="button"
            disabled={isSaving}
            onClick={() => save(true)}
            style={{ background: 'none', border: 'none', padding: '8px', fontSize: '13px', fontWeight: 600, color: 'var(--sarah-secondary)', cursor: 'pointer' }}
          >
            Skip — I’ll set this up later in Profile
          </button>
        </div>
      </form>
    </div>
  );
};
