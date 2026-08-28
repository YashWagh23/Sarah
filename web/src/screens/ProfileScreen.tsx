import React, { useState, useRef } from 'react';
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
  Clock
} from 'lucide-react';
import { useReminders } from '../context/RemindersContext';
import { useUserProfile } from '../context/UserProfileContext';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { exportAllDataJSON, importAllDataJSON } from '../lib/db';

export const ProfileScreen: React.FC = () => {
  const { notificationPermission, requestNotificationPermission, refreshReminders } = useReminders();
  const { profile, updateProfile } = useUserProfile();
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
  const [collegeEndTime, setCollegeEndTime] = useState(profile.collegeEndTime || '17:00');
  const [commuteMinutes, setCommuteMinutes] = useState(profile.commuteMinutes || 30);

  const fileInputRef = useRef<HTMLInputElement>(null);

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
    await updateProfile({
      targetBedtime,
      collegeEndTime,
      commuteMinutes: Number(commuteMinutes) || 0
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
      const dateStr = new Date().toISOString().split('T')[0];
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
      const result = await importAllDataJSON(text);
      if (result.success) {
        await refreshTasks();
        await refreshNotes();
        await refreshReminders();
        await refreshSubjects();
        showToast(`Restored: ${result.importedCounts.tasks} tasks, ${result.importedCounts.notes} notes, ${result.importedCounts.subjects} subjects! 🎉`);
      }
    } catch (err) {
      console.error(err);
      showToast('Invalid backup file');
    } finally {
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

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
          background: 'rgba(255, 255, 255, 0.94)'
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
                {profile.name}
              </div>
              <div style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)' }}>
                {profile.branch}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
                {profile.semester} • College Schedule Active
              </div>
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
                placeholder="e.g. Yash Wagh"
                style={{
                  padding: '8px 12px',
                  borderRadius: '10px',
                  border: '1px solid var(--sarah-outline-variant)',
                  fontSize: '13px',
                  outline: 'none',
                  backgroundColor: '#FFFFFF',
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
                  backgroundColor: '#FFFFFF',
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
                  backgroundColor: '#FFFFFF',
                  color: 'var(--sarah-on-background)'
                }}
              />
            </div>

            <button
              type="button"
              onClick={handleSaveProfile}
              className="btn-press"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                backgroundColor: 'var(--sarah-primary)',
                color: '#FFFFFF',
                border: 'none',
                borderRadius: '10px',
                padding: '9px',
                fontSize: '13px',
                fontWeight: 700,
                cursor: 'pointer',
                marginTop: '4px'
              }}
            >
              <Check size={15} />
              <span>Save Profile</span>
            </button>
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
              setCollegeEndTime(profile.collegeEndTime || '17:00');
              setCommuteMinutes(profile.commuteMinutes || 30);
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

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px' }}>
          <div style={{ display: 'flex', flexDirection: 'column', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 10px', borderRadius: '10px' }}>
            <span style={{ fontSize: '10.5px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>BEDTIME</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>{profile.targetBedtime || '23:30'}</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 10px', borderRadius: '10px' }}>
            <span style={{ fontSize: '10.5px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>COLLEGE END</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>{profile.collegeEndTime || '17:00'}</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', backgroundColor: 'var(--sarah-surface-container-low)', padding: '8px 10px', borderRadius: '10px' }}>
            <span style={{ fontSize: '10.5px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>COMMUTE</span>
            <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>{profile.commuteMinutes || 30}m</span>
          </div>
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
                  style={{ padding: '8px', borderRadius: '8px', border: '1px solid var(--sarah-outline-variant)', fontSize: '12.5px' }}
                />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>College End Time</label>
                <input
                  type="time"
                  value={collegeEndTime}
                  onChange={(e) => setCollegeEndTime(e.target.value)}
                  style={{ padding: '8px', borderRadius: '8px', border: '1px solid var(--sarah-outline-variant)', fontSize: '12.5px' }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>Commute Buffer (Minutes)</label>
              <input
                type="number"
                min={0}
                max={180}
                value={commuteMinutes}
                onChange={(e) => setCommuteMinutes(Number(e.target.value))}
                style={{ padding: '8px', borderRadius: '8px', border: '1px solid var(--sarah-outline-variant)', fontSize: '12.5px' }}
              />
            </div>

            <button
              type="button"
              onClick={handleSaveSchedule}
              className="btn-press"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
                backgroundColor: 'var(--sarah-primary)',
                color: '#FFFFFF',
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
        )}
      </div>

      {/* 3. Local Data Backup & Restore */}
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
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#059669', fontWeight: 600 }}>
            <CheckCircle2 size={13} />
            <span>100% Private</span>
          </div>
        </div>

        <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.45, margin: 0 }}>
          Export your complete Sarah academic records as a JSON backup, or restore data across devices.
        </p>

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

      {/* 4. Browser Notifications Card */}
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
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#059669', fontWeight: 600 }}>
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
              backgroundColor: 'rgba(68, 80, 183, 0.1)',
              color: 'var(--sarah-primary)',
              border: '1px solid rgba(68, 80, 183, 0.2)',
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

      {/* 5. iPhone Safari Install Guide Card */}
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
            <div style={{ width: '22px', height: '22px', borderRadius: '50%', backgroundColor: 'var(--sarah-primary)', color: '#FFFFFF', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
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
        <div style={{ width: '36px', height: '36px', borderRadius: '10px', overflow: 'hidden', boxShadow: '0 2px 8px rgba(68, 80, 183, 0.15)' }}>
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
