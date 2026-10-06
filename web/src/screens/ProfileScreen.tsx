import React, { useEffect, useRef, useState } from 'react';
import {
  BatteryCharging,
  Bell,
  ChevronRight,
  Download,
  Monitor,
  Moon,
  Pencil,
  PlusSquare,
  Share,
  Sun,
  Target,
  Upload,
  Zap
} from 'lucide-react';
import { useReminders } from '../context/RemindersContext';
import { useUserProfile } from '../context/UserProfileContext';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { exportAllDataJSON, importAllDataJSON, type EnergyLevel, type ImportMode, type ThemePreference } from '../lib/db';
import { formatMinutes, toLocalDateStr } from '../lib/datetime';
import { ENERGY_PROFILES, formatClock } from '../lib/planner';
import { CollegeDaysPicker, describeCollegeDays } from '../components/CollegeDaysPicker';
import { Field, FieldGroup, FormError, SectionHead, Segmented, Sheet } from '../components/ui';

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value));

function clockLabel(hhmm: string): string {
  const [h, m] = hhmm.split(':').map(Number);
  const d = new Date();
  d.setHours(h || 0, m || 0, 0, 0);
  return formatClock(d.getTime());
}

const SettingRow: React.FC<{ label: string; value: React.ReactNode; onClick?: () => void }> = ({ label, value, onClick }) => {
  const content = (
    <>
      <span className="grow" style={{ fontSize: 14.5 }}>{label}</span>
      <span className="mono" style={{ fontSize: 13.5, color: 'var(--text-2)' }}>{value}</span>
      {onClick && <ChevronRight size={17} color="var(--text-3)" />}
    </>
  );
  return onClick ? (
    <button type="button" className="list-row" style={{ minHeight: 50 }} onClick={onClick}>{content}</button>
  ) : (
    <div className="list-row" style={{ minHeight: 50 }}>{content}</div>
  );
};

export const ProfileScreen: React.FC = () => {
  const { notificationPermission, requestNotificationPermission, refreshReminders } = useReminders();
  const { profile, updateProfile, setEnergyLevel, setTheme, refreshProfile } = useUserProfile();
  const { refresh: refreshTasks, showToast } = useTasks();
  const { refreshNotes } = useNotes();
  const { refreshSubjects } = useSubjects();

  const [sheet, setSheet] = useState<'profile' | 'schedule' | null>(null);
  const [error, setError] = useState('');

  // Profile form
  const [name, setName] = useState(profile.name);
  const [branch, setBranch] = useState(profile.branch);
  const [semester, setSemester] = useState(profile.semester);

  // Schedule form
  const [targetBedtime, setTargetBedtime] = useState(profile.targetBedtime);
  const [wakeUpTime, setWakeUpTime] = useState(profile.wakeUpTime);
  const [collegeEndTime, setCollegeEndTime] = useState(profile.collegeEndTime);
  const [commuteMinutes, setCommuteMinutes] = useState(String(profile.commuteMinutes ?? 30));
  const [studyGoalHours, setStudyGoalHours] = useState(String(profile.dailyStudyGoalHours ?? 3));
  const [collegeDays, setCollegeDays] = useState<number[]>(profile.collegeDays ?? [1, 2, 3, 4, 5]);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importMode, setImportMode] = useState<ImportMode>('merge');

  // Re-seed a form from the saved profile each time its sheet opens.
  useEffect(() => {
    setError('');
    if (sheet === 'profile') {
      setName(profile.name);
      setBranch(profile.branch);
      setSemester(profile.semester);
    }
    if (sheet === 'schedule') {
      setTargetBedtime(profile.targetBedtime);
      setWakeUpTime(profile.wakeUpTime);
      setCollegeEndTime(profile.collegeEndTime);
      setCommuteMinutes(String(profile.commuteMinutes ?? 30));
      setStudyGoalHours(String(profile.dailyStudyGoalHours ?? 3));
      setCollegeDays(profile.collegeDays ?? [1, 2, 3, 4, 5]);
    }
    // Only when a sheet opens; profile edits elsewhere must not wipe a draft.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sheet]);

  const isStandalone = typeof window !== 'undefined'
    && (window.matchMedia?.('(display-mode: standalone)').matches || (navigator as Navigator & { standalone?: boolean }).standalone === true);

  const saveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Add your name so Sarah knows what to call you.');
      return;
    }
    await updateProfile({ name: name.trim(), branch: branch.trim(), semester: semester.trim() }, 'Profile saved');
    setSheet(null);
  };

  const saveSchedule = async (e: React.FormEvent) => {
    e.preventDefault();
    await updateProfile({
      targetBedtime,
      wakeUpTime,
      collegeEndTime,
      commuteMinutes: clamp(Number(commuteMinutes) || 0, 0, 180),
      dailyStudyGoalHours: clamp(Number(studyGoalHours) || 0, 0, 16),
      collegeDays
    }, 'Schedule saved. Tonight is re-planned.');
    setSheet(null);
  };

  const handleExport = async () => {
    try {
      const json = await exportAllDataJSON();
      const url = URL.createObjectURL(new Blob([json], { type: 'application/json' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `sarah_backup_${toLocalDateStr(new Date())}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      showToast('Backup downloaded');
    } catch (err) {
      console.error(err);
      showToast('Could not create the backup');
    }
  };

  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const result = await importAllDataJSON(await file.text(), importMode);
      if (result.success) {
        await Promise.all([refreshTasks(), refreshNotes(), refreshReminders(), refreshSubjects(), refreshProfile()]);
        const { tasks, notes, reminders, subjects } = result.importedCounts;
        showToast(`Restored ${tasks} tasks, ${notes} notes, ${reminders} reminders, ${subjects} subjects`);
      }
    } catch (err) {
      console.error(err);
      showToast(err instanceof Error ? err.message : 'Could not read that backup file');
    } finally {
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const initials = profile.name
    ? profile.name.trim().split(/\s+/).map(p => p[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  const themeOptions: Array<{ id: ThemePreference; label: string; icon: React.ComponentType<{ size?: number }> }> = [
    { id: 'system', label: 'System', icon: Monitor },
    { id: 'light', label: 'Light', icon: Sun },
    { id: 'dark', label: 'Dark', icon: Moon }
  ];
  const energyOptions: Array<{ id: EnergyLevel; label: string; icon: React.ComponentType<{ size?: number }> }> = [
    { id: 'high', label: ENERGY_PROFILES.high.label, icon: Zap },
    { id: 'normal', label: ENERGY_PROFILES.normal.label, icon: Target },
    { id: 'low', label: ENERGY_PROFILES.low.label, icon: BatteryCharging },
    { id: 'exhausted', label: ENERGY_PROFILES.exhausted.label, icon: Moon }
  ];

  const notificationStatus = {
    granted: 'On',
    denied: 'Blocked in browser',
    default: 'Off',
    unsupported: 'In-app only'
  }[notificationPermission];

  return (
    <div className="screen">
      <header className="row" style={{ gap: 14 }}>
        <span
          aria-hidden="true"
          className="row"
          style={{
            justifyContent: 'center',
            width: 56,
            height: 56,
            borderRadius: 'var(--r-surface)',
            background: 'var(--accent-soft)',
            color: 'var(--accent-text)',
            fontSize: 20,
            fontWeight: 700,
            flexShrink: 0
          }}
        >
          {initials}
        </span>
        <div className="grow">
          <h2 className="screen-title truncate">{profile.name || 'Your profile'}</h2>
          <p className="screen-sub truncate">
            {[profile.branch, profile.semester].filter(Boolean).join(', ') || 'Add your branch and semester'}
          </p>
        </div>
        <button type="button" className="icon-btn icon-btn-filled" aria-label="Edit profile" onClick={() => setSheet('profile')}>
          <Pencil size={17} />
        </button>
      </header>

      <section aria-labelledby="p-evening" className="stack-8">
        <SectionHead
          id="p-evening"
          title="Your day"
          action={<button type="button" className="btn btn-ghost btn-sm" onClick={() => setSheet('schedule')}>Edit</button>}
        />
        <div className="list">
          <SettingRow label="Classes end" value={clockLabel(profile.collegeEndTime)} onClick={() => setSheet('schedule')} />
          <SettingRow label="Commute" value={formatMinutes(profile.commuteMinutes ?? 0)} onClick={() => setSheet('schedule')} />
          <SettingRow label="Class days" value={describeCollegeDays(profile.collegeDays)} onClick={() => setSheet('schedule')} />
          <SettingRow label="Bedtime" value={clockLabel(profile.targetBedtime)} onClick={() => setSheet('schedule')} />
          <SettingRow label="Wake up" value={clockLabel(profile.wakeUpTime)} onClick={() => setSheet('schedule')} />
          <SettingRow label="Daily study goal" value={`${profile.dailyStudyGoalHours ?? 3}h`} onClick={() => setSheet('schedule')} />
        </div>
      </section>

      <section aria-labelledby="p-energy" className="stack-8">
        <SectionHead id="p-energy" title="Energy right now" />
        <Segmented<EnergyLevel> label="Energy level" value={profile.energyLevel} options={energyOptions} onChange={setEnergyLevel} />
        <p className="meta" style={{ padding: '0 2px' }}>{ENERGY_PROFILES[profile.energyLevel].hint}. Tonight's plan adjusts instantly.</p>
      </section>

      <section aria-labelledby="p-theme" className="stack-8">
        <SectionHead id="p-theme" title="Appearance" />
        <Segmented<ThemePreference> label="Theme" value={profile.theme || 'system'} options={themeOptions} onChange={setTheme} />
      </section>

      <section aria-labelledby="p-notify" className="stack-8">
        <SectionHead id="p-notify" title="Notifications" />
        <div className="list">
          <div className="list-row">
            <Bell size={18} color="var(--text-2)" />
            <span className="grow">
              <span style={{ display: 'block', fontSize: 14.5 }}>Reminder alerts</span>
              <span className="meta">Alerts fire while Sarah is open.</span>
            </span>
            {notificationPermission === 'default' ? (
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => requestNotificationPermission()}>Turn on</button>
            ) : (
              <span className={`tag ${notificationPermission === 'granted' ? 'tag-ok' : ''}`}>{notificationStatus}</span>
            )}
          </div>
        </div>
      </section>

      <section aria-labelledby="p-data" className="stack-8">
        <SectionHead id="p-data" title="Your data" />
        <p className="meta" style={{ padding: '0 2px', lineHeight: 1.5 }}>
          Everything stays on this device. Download a backup to move it to another phone or keep it safe.
        </p>
        <div className="form-grid-2">
          <button type="button" className="btn btn-secondary" onClick={handleExport}>
            <Download size={16} />
            Back up
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => fileInputRef.current?.click()}>
            <Upload size={16} />
            Restore
          </button>
        </div>
        <FieldGroup label="When restoring" hint={importMode === 'merge' ? 'Adds the backup to what is already here.' : 'Deletes everything here first, then restores.'}>
          <Segmented<ImportMode>
            label="Restore mode"
            value={importMode}
            onChange={setImportMode}
            options={[{ id: 'merge', label: 'Merge' }, { id: 'replace', label: 'Replace all' }]}
          />
        </FieldGroup>
        <input type="file" ref={fileInputRef} onChange={handleImport} accept=".json,application/json" hidden />
      </section>

      {!isStandalone && (
        <details className="panel" style={{ padding: '14px 16px' }}>
          <summary style={{ fontSize: 14.5, fontWeight: 600, cursor: 'pointer' }}>Install on your phone</summary>
          <ol style={{ margin: '12px 0 0 18px', display: 'grid', gap: 8, fontSize: 13.5, color: 'var(--text-2)', lineHeight: 1.5 }}>
            <li>Open Sarah in Safari (iPhone) or Chrome (Android).</li>
            <li className="row" style={{ gap: 4, display: 'list-item' }}>
              Tap Share <Share size={13} style={{ verticalAlign: '-2px' }} /> or the browser menu.
            </li>
            <li>
              Choose Add to Home Screen <PlusSquare size={13} style={{ verticalAlign: '-2px' }} />.
            </li>
            <li>Open Sarah from your home screen. It works offline.</li>
          </ol>
        </details>
      )}

      <footer className="row-8" style={{ justifyContent: 'center', color: 'var(--text-3)', fontSize: 12.5, paddingTop: 4 }}>
        <img src="./sarah_logo.png" alt="" width={20} height={20} style={{ borderRadius: 6 }} />
        Sarah keeps your data on this device.
      </footer>

      {/* Edit profile */}
      <Sheet
        open={sheet === 'profile'}
        title="Edit profile"
        onClose={() => setSheet(null)}
        onSubmit={saveProfile}
        footer={<button type="submit" className="btn btn-primary btn-lg">Save profile</button>}
      >
        <FormError message={error} />
        <Field label="Name">
          <input className="input" value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" placeholder="Priya Sharma" />
        </Field>
        <Field label="Branch or major">
          <input className="input" value={branch} onChange={(e) => setBranch(e.target.value)} placeholder="Computer Engineering" />
        </Field>
        <Field label="Semester">
          <input className="input" value={semester} onChange={(e) => setSemester(e.target.value)} placeholder="Semester 5" />
        </Field>
      </Sheet>

      {/* Edit schedule */}
      <Sheet
        open={sheet === 'schedule'}
        title="Your day"
        onClose={() => setSheet(null)}
        onSubmit={saveSchedule}
        footer={<button type="submit" className="btn btn-primary btn-lg">Save schedule</button>}
      >
        <div className="form-grid-2">
          <Field label="Classes end">
            <input className="input" type="time" value={collegeEndTime} onChange={(e) => setCollegeEndTime(e.target.value)} />
          </Field>
          <Field label="Commute (min)">
            <input className="input" type="number" inputMode="numeric" min={0} max={180} value={commuteMinutes} onChange={(e) => setCommuteMinutes(e.target.value)} />
          </Field>
          <Field label="Bedtime">
            <input className="input" type="time" value={targetBedtime} onChange={(e) => setTargetBedtime(e.target.value)} />
          </Field>
          <Field label="Wake up">
            <input className="input" type="time" value={wakeUpTime} onChange={(e) => setWakeUpTime(e.target.value)} />
          </Field>
        </div>
        <FieldGroup label="Class days" hint="On other days the whole afternoon is free.">
          <CollegeDaysPicker value={collegeDays} onChange={setCollegeDays} />
        </FieldGroup>
        <Field label="Daily study goal (hours)">
          <input className="input" type="number" inputMode="decimal" min={0} max={16} step={0.5} value={studyGoalHours} onChange={(e) => setStudyGoalHours(e.target.value)} />
        </Field>
      </Sheet>
    </div>
  );
};
