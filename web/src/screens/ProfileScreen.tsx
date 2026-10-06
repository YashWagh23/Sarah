import React, { useState, useRef, useEffect } from 'react';
import { 
  Smartphone, 
  Share2, 
  PlusSquare, 
  CheckCircle2, 
  Bell, 
  BellRing, 
  AlertTriangle, 
  ShieldCheck, 
  Sparkles,
  Edit3,
  Check,
  X,
  Download,
  Upload,
  Clock,
  Sun,
  Moon,
  Monitor,
  Zap,
  Target,
  BatteryCharging
} from 'lucide-react';
import { useReminders } from '../context/RemindersContext';
import { useUserProfile } from '../context/UserProfileContext';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { exportAllDataJSON, importAllDataJSON, type EnergyLevel, type ImportMode, type ThemePreference } from '../lib/db';
import { toLocalDateStr } from '../lib/datetime';
import { ENERGY_PROFILES } from '../lib/planner';
import { CollegeDaysPicker, describeCollegeDays } from '../components/CollegeDaysPicker';

export const ProfileScreen: React.FC = () => {
  const { notificationPermission, requestNotificationPermission, refreshReminders } = useReminders();
  const { profile, updateProfile, setEnergyLevel, setTheme, studyGoal, refreshProfile } = useUserProfile();
  const { refresh: refreshTasks, showToast } = useTasks();
  const { refreshNotes } = useNotes();
  const { refreshSubjects } = useSubjects();

  // Profile Edit State
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [name, setName] = useState(profile.name);
  const [branch, setBranch] = useState(profile.branch);
  const [semester, setSemester] = useState(profile.semester);

  // Schedule Edit State
  const [isEditingSchedule, setIsEditingSchedule] = useState(false);
  const [targetBedtime, setTargetBedtime] = useState(profile.targetBedtime || '23:30');
  const [wakeUpTime, setWakeUpTime] = useState(profile.wakeUpTime || '07:00');
  const [collegeEndTime, setCollegeEndTime] = useState(profile.collegeEndTime || '17:00');
  const [commuteMinutes, setCommuteMinutes] = useState(profile.commuteMinutes || 30);
  const [dailyStudyGoalHours, setDailyStudyGoalHours] = useState(profile.dailyStudyGoalHours ?? 3);
  const [collegeDays, setCollegeDays] = useState<number[]>(profile.collegeDays ?? [1, 2, 3, 4, 5]);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [importMode, setImportMode] = useState<ImportMode>('merge');

  useEffect(() => {
    if (!isEditingProfile) {
      setName(profile.name);
      setBranch(profile.branch);
      setSemester(profile.semester);
    }
  }, [profile, isEditingProfile]);

  useEffect(() => {
    if (!isEditingSchedule) {
      setTargetBedtime(profile.targetBedtime || '23:30');
      setWakeUpTime(profile.wakeUpTime || '07:00');
      setCollegeEndTime(profile.collegeEndTime || '17:00');
      setCommuteMinutes(profile.commuteMinutes || 30);
      setDailyStudyGoalHours(profile.dailyStudyGoalHours ?? 3);
      setCollegeDays(profile.collegeDays ?? [1, 2, 3, 4, 5]);
    }
  }, [profile, isEditingSchedule]);

  const handleSaveProfile = async () => {
    if (!name.trim()) {
      showToast('Name cannot be empty');
      return;
    }
    await updateProfile({
      name: name.trim(),
      branch: branch.trim(),
      semester: semester.trim()
    });
    setIsEditingProfile(false);
  };

  const handleSaveSchedule = async () => {
    const clamp = (value: number, min: number, max: number) =>
      Math.min(max, Math.max(min, value));

    await updateProfile({
      targetBedtime,
      wakeUpTime,
      collegeEndTime,
      commuteMinutes: clamp(Number(commuteMinutes) || 0, 0, 180),
      dailyStudyGoalHours: clamp(Number(dailyStudyGoalHours) || 0, 0, 16),
      collegeDays
    });
    setIsEditingSchedule(false);
  };

  // Export JSON Backup
  const handleExportBackup = async () => {
    try {
      const jsonStr = await exportAllDataJSON();
      const blob = new Blob([jsonStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      const dateStr = toLocalDateStr(new Date());
      a.href = url;
      a.download = `sarah_academic_backup_${dateStr}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      showToast('Backup exported successfully 📦');
    } catch (err) {
      console.error(err);
      showToast('Failed to export backup');
    }
  };

  // Import JSON Backup
  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const text = await file.text();
      const result = await importAllDataJSON(text, importMode);
      if (result.success) {
        await refreshTasks();
        await refreshNotes();
        await refreshReminders();
        await refreshSubjects();
        await refreshProfile();
        const { tasks, notes, reminders, subjects } = result.importedCounts;
        showToast(`Restored ${tasks} tasks, ${notes} notes, ${reminders} reminders, ${subjects} subjects`);
      }
    } catch (err) {
      console.error(err);
      // The importer reports exactly why a file was rejected.
      showToast(err instanceof Error ? err.message : 'Could not read that backup file');
    } finally {
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const themeOptions: Array<{ id: ThemePreference; label: string; icon: React.ComponentType<{ size?: number }> }> = [
    { id: 'system', label: 'System', icon: Monitor },
    { id: 'light', label: 'Light', icon: Sun },
    { id: 'dark', label: 'Dark', icon: Moon }
  ];

  const energyOptions: Array<{ id: EnergyLevel; label: string; hint: string; icon: React.ComponentType<{ size?: number }> }> = [
    { id: 'high', label: ENERGY_PROFILES.high.label, hint: '60m blocks', icon: Zap },
    { id: 'normal', label: ENERGY_PROFILES.normal.label, hint: '45m blocks', icon: Target },
    { id: 'low', label: ENERGY_PROFILES.low.label, hint: '30m blocks', icon: BatteryCharging },
    { id: 'exhausted', label: ENERGY_PROFILES.exhausted.label, hint: 'Urgent only', icon: Moon }
  ];

  const scheduleFieldStyle: React.CSSProperties = {
    padding: '8px',
    borderRadius: '8px',
    border: '1px solid var(--sarah-outline-variant)',
    fontSize: '12.5px',
    backgroundColor: 'var(--sarah-surface-card)',
    color: 'var(--sarah-on-background)'
  };

  const scheduleTiles = [
    { label: 'BEDTIME', value: profile.targetBedtime || '23:30' },
    { label: 'WAKE UP', value: profile.wakeUpTime || '07:00' },
    { label: 'COLLEGE END', value: profile.collegeEndTime || '17:00' },
    { label: 'COMMUTE', value: (profile.commuteMinutes ?? 30) + 'm' },
    { label: 'CLASSES', value: describeCollegeDays(profile.collegeDays) },
    { label: 'STUDY GOAL', value: (profile.dailyStudyGoalHours ?? 3) + 'h' },
    { label: 'DONE TODAY', value: Math.round(studyGoal.completedMinutes / 6) / 10 + 'h' },
    { label: 'ENERGY', value: ENERGY_PROFILES[profile.energyLevel]?.label ?? 'Steady' }
  ];

  const initials = profile.name
    ? profile.name.trim().split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase()
    : 'S';

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '18px'
      }}
    >
      <div>
        <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0 }}>
          Profile & Settings
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
          Personal college assistant preferences
        </p>
      </div>

      {/* 1. User Profile Card */}
      <div
        className="glass-card"
        style={{
          padding: '18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '14px',
          background: 'var(--glass-bg-strong)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div
              style={{
                width: '52px',
                height: '52px',
                borderRadius: '16px',
                backgroundColor: 'var(--sarah-primary-fixed)',
                color: 'var(--sarah-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '19px',
                fontWeight: 800
              }}
            >
              {initials}
            </div>
            <div>
              <div style={{ fontSize: '17px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
                {profile.name || 'Add your name'}
              </div>
              <div style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)' }}>
                {profile.branch || 'Branch / major not set'}
              </div>
              {profile.semester && (
                <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
                  {profile.semester}
                </div>
              )}
            </div>
          </div>

          <button
            type="button"
            aria-label="Edit Profile"
            onClick={() => {
              setName(profile.name);
              setBranch(profile.branch);
              setSemester(profile.semester);
              setIsEditingProfile(prev => !prev);
            }}
            className="btn-press"
            style={{
              background: isEditingProfile ? 'var(--sarah-primary-fixed)' : 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '10px',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: 'var(--sarah-primary)'
            }}
          >
            {isEditingProfile ? <X size={16} /> : <Edit3 size={15} />}
          </button>
        </div>

        {/* Profile Inline Editor */}
        {isEditingProfile && (
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              paddingTop: '8px',
              borderTop: '1px solid var(--sarah-outline-variant)'
            }}
          >
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label style={{ fontSize: '11.5px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>
                Full Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Priya Sharma"
                style={{
                  padding: '8px 12px',
                  borderRadius: '10px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13px',
                  outline: 'none',
                  backgroundColor: 'var(--sarah-surface-card)',
                  color: 'var(--sarah-on-background)'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label style={{ fontSize: '11.5px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>
                Branch / Major
              </label>
              <input
                type="text"
                value={branch}
                onChange={(e) => setBranch(e.target.value)}
                placeholder="e.g. Computer Science & Engineering"
                style={{
                  padding: '8px 12px',
                  borderRadius: '10px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13px',
                  outline: 'none',
                  backgroundColor: 'var(--sarah-surface-card)',
                  color: 'var(--sarah-on-background)'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label style={{ fontSize: '11.5px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>
                Current Semester
              </label>
              <input
                type="text"
                value={semester}
                onChange={(e) => setSemester(e.target.value)}
                placeholder="e.g. Semester 6"
                style={{
                  padding: '8px 12px',
                  borderRadius: '10px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13px',
                  outline: 'none',
                  backgroundColor: 'var(--sarah-surface-card)',
                  color: 'var(--sarah-on-background)'
                }}
              />
            </div>

            <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
              <button
                type="button"
                onClick={() => setIsEditingProfile(false)}
                className="btn-press"
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-secondary)',
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '10px',
                  padding: '9px',
                  fontSize: '13px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                <span>Cancel</span>
              </button>
              <button
                type="button"
                onClick={handleSaveProfile}
                className="btn-press"
                style={{
                  flex: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  backgroundColor: 'var(--sarah-primary)',
                  color: 'var(--sarah-on-primary)',
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px',
                  fontSize: '13px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                <Check size={15} />
                <span>Save Profile</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* 2. Daily Academic Schedule & Feasibility Settings */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Clock size={18} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Academic & Bedtime Schedule
            </span>
          </div>

          <button
            type="button"
            aria-label="Edit Schedule"
            onClick={() => {
              setTargetBedtime(profile.targetBedtime || '23:30');
              setWakeUpTime(profile.wakeUpTime || '07:00');
              setCollegeEndTime(profile.collegeEndTime || '17:00');
              setCommuteMinutes(profile.commuteMinutes || 30);
              setDailyStudyGoalHours(profile.dailyStudyGoalHours ?? 3);
              setCollegeDays(profile.collegeDays ?? [1, 2, 3, 4, 5]);
              setIsEditingSchedule(prev => !prev);
            }}
            className="btn-press"
            style={{
              background: isEditingSchedule ? 'var(--sarah-primary-fixed)' : 'var(--sarah-surface-container-low)',
              border: 'none',
              borderRadius: '10px',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: 'var(--sarah-primary)'
            }}
          >
            {isEditingSchedule ? <X size={16} /> : <Edit3 size={15} />}
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
          {scheduleTiles.map((tile) => (
            <div
              key={tile.label}
              style={{ display: 'flex', flexDirection: 'column', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 10px', borderRadius: '10px' }}
            >
              <span style={{ fontSize: '10.5px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>{tile.label}</span>
              <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>{tile.value}</span>
            </div>
          ))}
        </div>

        {isEditingSchedule && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', paddingTop: '6px', borderTop: '1px solid var(--sarah-outline-variant)' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Target Bedtime</label>
                <input
                  type="time"
                  value={targetBedtime}
                  onChange={(e) => setTargetBedtime(e.target.value)}
                  style={scheduleFieldStyle}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Wake-Up Time</label>
                <input
                  type="time"
                  value={wakeUpTime}
                  onChange={(e) => setWakeUpTime(e.target.value)}
                  style={scheduleFieldStyle}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>College End Time</label>
                <input
                  type="time"
                  value={collegeEndTime}
                  onChange={(e) => setCollegeEndTime(e.target.value)}
                  style={scheduleFieldStyle}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Commute Buffer (min)</label>
                <input
                  type="number"
                  inputMode="numeric"
                  min={0}
                  max={180}
                  value={commuteMinutes}
                  onChange={(e) => setCommuteMinutes(Number(e.target.value))}
                  style={scheduleFieldStyle}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Daily Study Goal (hrs)</label>
                <input
                  type="number"
                  inputMode="decimal"
                  min={0}
                  max={16}
                  step={0.5}
                  value={dailyStudyGoalHours}
                  onChange={(e) => setDailyStudyGoalHours(Number(e.target.value))}
                  style={scheduleFieldStyle}
                />
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Days with classes</label>
              <CollegeDaysPicker value={collegeDays} onChange={setCollegeDays} />
            </div>

            <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
              <button
                type="button"
                onClick={() => setIsEditingSchedule(false)}
                className="btn-press"
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: 'var(--sarah-surface-container-low)',
                  color: 'var(--sarah-secondary)',
                  border: '1px solid var(--sarah-outline-variant)',
                  borderRadius: '10px',
                  padding: '9px',
                  fontSize: '13px',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                <span>Cancel</span>
              </button>
              <button
                type="button"
                onClick={handleSaveSchedule}
                className="btn-press"
                style={{
                  flex: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  backgroundColor: 'var(--sarah-primary)',
                  color: 'var(--sarah-on-primary)',
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px',
                  fontSize: '13px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                <Check size={15} />
                <span>Save Schedule</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* 3. Appearance */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Sun size={18} color="var(--sarah-primary)" />
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            Appearance
          </span>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          Sarah ships with a light theme and an obsidian dark theme. Following the system switches automatically with your device.
        </p>

        <div
          role="radiogroup"
          aria-label="Theme"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '6px',
            backgroundColor: 'var(--sarah-surface-container-low)',
            padding: '4px',
            borderRadius: '14px'
          }}
        >
          {themeOptions.map((opt) => {
            const isSelected = (profile.theme || 'system') === opt.id;
            const Icon = opt.icon;
            return (
              <button
                key={opt.id}
                type="button"
                role="radio"
                aria-checked={isSelected}
                onClick={() => setTheme(opt.id)}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '9px 4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  backgroundColor: isSelected ? 'var(--sarah-segment-active)' : 'transparent',
                  color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: isSelected ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12.5px',
                  fontWeight: isSelected ? 700 : 500,
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <Icon size={14} />
                <span>{opt.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Energy Level */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Zap size={18} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Energy Level
            </span>
          </div>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>
            Shapes tonight&rsquo;s plan
          </span>
        </div>

        <div
          role="radiogroup"
          aria-label="Energy level"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: '6px',
            backgroundColor: 'var(--sarah-surface-container-low)',
            padding: '4px',
            borderRadius: '14px'
          }}
        >
          {energyOptions.map((opt) => {
            const isSelected = profile.energyLevel === opt.id;
            const Icon = opt.icon;
            return (
              <button
                key={opt.id}
                type="button"
                role="radio"
                aria-checked={isSelected}
                onClick={() => setEnergyLevel(opt.id)}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '8px 4px',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '2px',
                  backgroundColor: isSelected ? 'var(--sarah-segment-active)' : 'transparent',
                  color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: isSelected ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12px',
                  fontWeight: isSelected ? 700 : 500,
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <Icon size={14} />
                <span>{opt.label}</span>
                <span style={{ fontSize: '9.5px', fontWeight: 500, color: 'var(--sarah-secondary)' }}>{opt.hint}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 5. Local Data Backup & Restore */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShieldCheck size={18} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Data Backup & Restore
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-success)', fontWeight: 600 }}>
            <CheckCircle2 size={13} />
            <span>100% Private</span>
          </div>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          Export your complete Sarah academic records as a JSON backup, or restore data across devices.
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <span style={{ fontSize: '10.5px', fontWeight: 700, letterSpacing: '0.05em', textTransform: 'uppercase', color: 'var(--sarah-secondary)' }}>
            Restore Mode
          </span>
          <div
            role="radiogroup"
            aria-label="Restore mode"
            style={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '6px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              padding: '4px',
              borderRadius: '12px'
            }}
          >
            {([
              { id: 'merge' as ImportMode, label: 'Merge', hint: 'Keeps what is already here' },
              { id: 'replace' as ImportMode, label: 'Replace', hint: 'Wipes local data first' }
            ]).map((opt) => {
              const isSelected = importMode === opt.id;
              return (
                <button
                  key={opt.id}
                  type="button"
                  role="radio"
                  aria-checked={isSelected}
                  onClick={() => setImportMode(opt.id)}
                  className="btn-press"
                  style={{
                    border: 'none',
                    borderRadius: '9px',
                    padding: '7px 6px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '1px',
                    backgroundColor: isSelected ? 'var(--sarah-segment-active)' : 'transparent',
                    color: isSelected
                      ? (opt.id === 'replace' ? 'var(--sarah-error)' : 'var(--sarah-primary)')
                      : 'var(--sarah-on-surface-variant)',
                    boxShadow: isSelected ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                    fontSize: '12px',
                    fontWeight: isSelected ? 700 : 500,
                    cursor: 'pointer'
                  }}
                >
                  <span>{opt.label}</span>
                  <span style={{ fontSize: '9.5px', fontWeight: 500, color: 'var(--sarah-secondary)' }}>{opt.hint}</span>
                </button>
              );
            })}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
          <button
            type="button"
            onClick={handleExportBackup}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-on-background)',
              border: '1px solid var(--sarah-outline-variant)',
              borderRadius: '12px',
              padding: '10px',
              fontSize: '12.5px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <Download size={15} color="var(--sarah-primary)" />
            <span>Export Backup</span>
          </button>

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-on-background)',
              border: '1px solid var(--sarah-outline-variant)',
              borderRadius: '12px',
              padding: '10px',
              fontSize: '12.5px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <Upload size={15} color="var(--sarah-primary)" />
            <span>Restore Backup</span>
          </button>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept=".json"
            style={{ display: 'none' }}
          />
        </div>
      </div>

      {/* 6. Browser Notifications Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Bell size={17} color="var(--sarah-primary)" />
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              Notifications & Alerts
            </span>
          </div>

          {/* Status Chip */}
          {notificationPermission === 'granted' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-success)', fontWeight: 600 }}>
              <CheckCircle2 size={13} />
              <span>Enabled</span>
            </div>
          )}
          {notificationPermission === 'denied' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-error)', fontWeight: 600 }}>
              <AlertTriangle size={13} />
              <span>Blocked</span>
            </div>
          )}
          {notificationPermission === 'default' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              <span>Not Requested</span>
            </div>
          )}
          {notificationPermission === 'unsupported' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              <span>In-App Alerts Only</span>
            </div>
          )}
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          Sarah delivers quiet reminders and alerts when deadlines and study sessions arrive while the app is active.
        </p>

        {notificationPermission === 'default' && (
          <button
            type="button"
            onClick={() => requestNotificationPermission()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              backgroundColor: 'rgba(var(--sarah-primary-rgb), 0.1)',
              color: 'var(--sarah-primary)',
              border: '1px solid rgba(var(--sarah-primary-rgb), 0.2)',
              borderRadius: '12px',
              padding: '10px 14px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <BellRing size={15} />
            <span>Enable Notifications</span>
          </button>
        )}

        {notificationPermission === 'granted' && (
          <div style={{ fontSize: '11.5px', color: 'var(--sarah-secondary)', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 12px', borderRadius: '10px' }}>
            ✓ Browser notifications are authorized and active.
          </div>
        )}
      </div>

      {/* 7. iPhone Safari Install Guide Card */}
      <div
        className="surface-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Smartphone size={18} color="var(--sarah-primary)" />
          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            Install on iPhone
          </span>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          For the full standalone Apple app experience without Safari browser bars:
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '9px', fontSize: '12.5px', color: 'var(--sarah-on-background)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              1
            </div>
            <div>
              Open Sarah in <strong>Safari</strong> on your iPhone
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              2
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Tap Safari's <Share2 size={13} color="var(--sarah-primary)" /> <strong>Share</strong> button
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              3
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              Select <PlusSquare size={13} color="var(--sarah-primary)" /> <strong>Add to Home Screen</strong>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-surface-container-high)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              4
            </div>
            <div>
              Tap <strong>Add</strong> in the top-right corner
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-primary)', color: 'var(--sarah-on-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
              5
            </div>
            <div>
              Launch <strong>Sarah</strong> directly from your Home Screen
            </div>
          </div>
        </div>
      </div>

      {/* Sarah Info & Branding */}
      <div style={{ textAlign: 'center', padding: '10px 0', color: 'var(--sarah-secondary)', fontSize: '11px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px' }}>
        <div style={{ width: '36px', height: '36px', borderRadius: '10px', overflow: 'hidden', boxShadow: '0 2px 8px rgba(var(--sarah-primary-rgb), 0.15)' }}>
          <img src="./sarah_logo.png" alt="Sarah Logo" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', fontWeight: 700, color: 'var(--sarah-primary)' }}>
          <Sparkles size={13} />
          <span>Sarah • Personal College Assistant</span>
        </div>
        <div>
          Apple-Inspired Progressive Web App • Offline Ready
        </div>
      </div>
    </div>
  );
};
