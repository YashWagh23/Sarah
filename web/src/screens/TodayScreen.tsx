import React from 'react';
import { 
  Sparkles, 
  Clock, 
  CheckCircle2, 
  Plus, 
  Flame, 
  BookOpen, 
  Coffee, 
  Check, 
  Pin, 
  ArrowRight,
  Bell,
  Zap,
  Target,
  BatteryCharging,
  Moon
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';
import { useSubjects } from '../context/SubjectsContext';
import { useUserProfile } from '../context/UserProfileContext';
import { TaskCard } from '../components/TaskCard';
import { NoteCard } from '../components/NoteCard';
import { ReminderCard } from '../components/ReminderCard';
import { type EnergyLevel } from '../lib/db';

interface TodayScreenProps {
  onNavigateToNotes?: () => void;
}

export const TodayScreen: React.FC<TodayScreenProps> = ({ onNavigateToNotes }) => {
  const { 
    activeTasks, 
    completedTasks, 
    mustDoTasks, 
    shouldDoTasks, 
    laterTasks, 
    nextActionTask, 
    toggleTaskCompletion, 
    openEditTaskModal, 
    openCreateTaskModal 
  } = useTasks();

  const {
    pinnedNotes,
    notes,
    openEditNoteModal,
    togglePin,
    removeNote
  } = useNotes();

  const {
    activeReminders,
    dismiss,
    snooze,
    openEditReminderModal,
    openCreateReminderModal,
    removeReminder
  } = useReminders();

  const { getSubjectColor } = useSubjects();
  const { profile, setEnergyLevel, feasibility } = useUserProfile();

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  const todayStr = new Date().toISOString().split('T')[0];
  const dueTodayCount = activeTasks.filter(t => t.deadline <= todayStr).length;
  const totalEstMinutes = activeTasks.reduce((acc, t) => acc + (t.estimatedMinutes || 0), 0);

  // Take up to 3 pinned notes for Today preview
  const previewPinnedNotes = pinnedNotes.slice(0, 3);
  // Take top active reminders for Today
  const upcomingRemindersPreview = activeReminders.slice(0, 4);

  const firstName = profile.name ? profile.name.trim().split(' ')[0] : 'Student';

  const energyOptions: Array<{ id: EnergyLevel; label: string; icon: React.ComponentType<{ size?: number }> }> = [
    { id: 'high', label: 'High', icon: Zap },
    { id: 'normal', label: 'Steady', icon: Target },
    { id: 'low', label: 'Low', icon: BatteryCharging },
    { id: 'exhausted', label: 'Rest', icon: Moon }
  ];

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
      {/* 1. Sarah Header & Dynamic Greeting */}
      <section style={{ display: 'flex', flexDirection: 'column', gap: '4px', paddingTop: '2px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <h2
              style={{
                fontSize: '24px',
                fontWeight: 800,
                color: 'var(--sarah-on-background)',
                letterSpacing: '-0.03em',
                margin: 0
              }}
            >
              {getGreeting()}, {firstName}
            </h2>
            <p
              style={{
                fontSize: '13px',
                color: 'var(--sarah-on-surface-variant)',
                margin: 0
              }}
            >
              Bedtime target: {profile.targetBedtime || '23:30'} • Academic OS
            </p>
          </div>
        </div>
      </section>

      {/* 2. 4-State Dynamic Energy Model Picker */}
      <section
        className="surface-card"
        style={{
          padding: '10px 12px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px',
          backgroundColor: '#FFFFFF'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 4px' }}>
          <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase', color: 'var(--sarah-secondary)' }}>
            Current Energy Level
          </span>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-primary)' }}>
            {profile.energyLevel === 'high' && '⚡ 1.25x Focus Multiplier'}
            {profile.energyLevel === 'normal' && '🎯 1.0x Standard Pace'}
            {profile.energyLevel === 'low' && '🔋 0.7x Reduced Capacity'}
            {profile.energyLevel === 'exhausted' && '🛋️ 0.4x Rest Mode'}
          </span>
        </div>

        <div
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
                onClick={() => setEnergyLevel(opt.id)}
                className="btn-press"
                style={{
                  border: 'none',
                  borderRadius: '10px',
                  padding: '7px 4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px',
                  backgroundColor: isSelected ? '#FFFFFF' : 'transparent',
                  color: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-on-surface-variant)',
                  boxShadow: isSelected ? '0 2px 8px rgba(0,0,0,0.06)' : 'none',
                  fontSize: '12px',
                  fontWeight: isSelected ? 700 : 500,
                  cursor: 'pointer',
                  transition: 'all 0.15s ease'
                }}
              >
                <Icon size={13} />
                <span>{opt.label}</span>
              </button>
            );
          })}
        </div>
      </section>

      {/* 3. Deterministic Bedtime Feasibility Engine Card */}
      <section
        className="glass-card"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px',
          backgroundColor: feasibility.status === 'optimal' 
            ? 'rgba(255, 255, 255, 0.92)' 
            : feasibility.status === 'tight' 
            ? 'rgba(255, 255, 255, 0.95)'
            : 'rgba(255, 245, 245, 0.92)',
          border: feasibility.status === 'overloaded' 
            ? '1px solid rgba(186, 26, 26, 0.25)' 
            : '1px solid var(--glass-border)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Sparkles size={15} color="var(--sarah-primary)" />
            <span style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.07em', textTransform: 'uppercase', color: 'var(--sarah-primary)' }}>
              Feasibility Engine
            </span>
          </div>

          <span
            style={{
              fontSize: '10.5px',
              fontWeight: 700,
              padding: '3px 8px',
              borderRadius: '8px',
              backgroundColor: feasibility.status === 'optimal'
                ? 'rgba(16, 185, 129, 0.12)'
                : feasibility.status === 'tight'
                ? 'rgba(245, 158, 11, 0.12)'
                : feasibility.status === 'rest_recommended'
                ? 'rgba(139, 92, 246, 0.12)'
                : 'rgba(186, 26, 26, 0.12)',
              color: feasibility.status === 'optimal'
                ? '#059669'
                : feasibility.status === 'tight'
                ? 'var(--sarah-tertiary)'
                : feasibility.status === 'rest_recommended'
                ? '#7C3AED'
                : 'var(--sarah-error)'
            }}
          >
            {feasibility.status === 'optimal' && 'CAPACITY OPTIMAL'}
            {feasibility.status === 'tight' && 'TIGHT SCHEDULE'}
            {feasibility.status === 'overloaded' && 'OVERLOADED'}
            {feasibility.status === 'rest_recommended' && 'REST MODE'}
          </span>
        </div>

        <div>
          <h3 style={{ fontSize: '14.5px', fontWeight: 700, color: 'var(--sarah-on-background)', margin: '0 0 2px 0' }}>
            {feasibility.headline}
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', margin: 0, lineHeight: 1.45 }}>
            {feasibility.subtext}
          </p>
        </div>
      </section>

      {/* 2. Sarah's Next Move — Liquid Glass Card */}
      {nextActionTask ? (
        <section 
          className="glass-card btn-press"
          onClick={() => openEditTaskModal(nextActionTask)}
          style={{
            padding: '18px 20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '14px',
            position: 'relative',
            overflow: 'hidden',
            cursor: 'pointer'
          }}
        >
          {/* Ambient Radial Accent Glow */}
          <div 
            style={{
              position: 'absolute',
              top: '-25px',
              right: '-25px',
              width: '130px',
              height: '130px',
              borderRadius: '50%',
              background: 'radial-gradient(circle, rgba(94, 106, 210, 0.22) 0%, rgba(255,255,255,0) 70%)',
              pointerEvents: 'none'
            }}
          />

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '7px' }}>
              <Sparkles size={16} color="var(--sarah-primary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-primary)'
                }}
              >
                Sarah's Next Move
              </span>
            </div>

            {/* Urgency Badge */}
            {nextActionTask.deadline < todayStr ? (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 700,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(186, 26, 26, 0.12)',
                  color: 'var(--sarah-error)'
                }}
              >
                OVERDUE
              </span>
            ) : nextActionTask.priority === 'must' ? (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 700,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(186, 26, 26, 0.1)',
                  color: 'var(--sarah-error)'
                }}
              >
                MUST DO TONIGHT
              </span>
            ) : (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 600,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(68, 80, 183, 0.1)',
                  color: 'var(--sarah-primary)'
                }}
              >
                RECOMMENDED
              </span>
            )}
          </div>

          <div>
            <h3
              style={{
                fontSize: '18px',
                fontWeight: 700,
                color: 'var(--sarah-on-background)',
                letterSpacing: '-0.02em',
                marginBottom: '4px'
              }}
            >
              {nextActionTask.title}
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span
                style={{
                  width: '6px',
                  height: '6px',
                  borderRadius: '50%',
                  backgroundColor: getSubjectColor(nextActionTask.subject)
                }}
              />
              <span style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', fontWeight: 500 }}>
                {nextActionTask.subject}
              </span>
              {nextActionTask.description && (
                <>
                  <span style={{ fontSize: '10px', color: 'var(--sarah-outline)' }}>•</span>
                  <span style={{ fontSize: '12px', color: 'var(--sarah-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {nextActionTask.description}
                  </span>
                </>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '10px', paddingTop: '2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--sarah-secondary)', fontSize: '12px', fontWeight: 500 }}>
              <Clock size={14} />
              <span>{nextActionTask.estimatedMinutes} mins session</span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  toggleTaskCompletion(nextActionTask.id);
                }}
                className="btn-press"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  background: 'var(--sarah-primary)',
                  color: '#FFFFFF',
                  border: 'none',
                  borderRadius: '12px',
                  padding: '8px 16px',
                  fontSize: '13px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  boxShadow: '0 4px 12px rgba(68, 80, 183, 0.3)'
                }}
              >
                <Check size={14} strokeWidth={2.5} />
                <span>Mark Done</span>
              </button>
            </div>
          </div>
        </section>
      ) : (
        /* All caught up card */
        <section
          className="glass-card"
          style={{
            padding: '22px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '16px',
            backgroundColor: 'rgba(255, 255, 255, 0.88)'
          }}
        >
          <div
            style={{
              width: '46px',
              height: '46px',
              borderRadius: '14px',
              backgroundColor: 'rgba(16, 185, 129, 0.12)',
              color: '#059669',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}
          >
            <Coffee size={24} />
          </div>
          <div>
            <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              All Caught Up! 🌟
            </div>
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-on-surface-variant)', marginTop: '2px' }}>
              No active tasks remaining tonight. Enjoy the breathing room.
            </div>
          </div>
        </section>
      )}

      {/* 3. Daily Summary Stats Bar */}
      <section
        className="surface-card"
        style={{
          padding: '12px 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          backgroundColor: '#FFFFFF'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: 'var(--sarah-primary)' }}>
            {dueTodayCount}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Due Today
          </span>
        </div>

        <div style={{ width: '1px', height: '26px', backgroundColor: 'var(--sarah-outline-variant)' }} />

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: '#059669' }}>
            {completedTasks.length}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Completed
          </span>
        </div>

        <div style={{ width: '1px', height: '26px', backgroundColor: 'var(--sarah-outline-variant)' }} />

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: 'var(--sarah-on-background)' }}>
            {totalEstMinutes > 0 ? `${totalEstMinutes}m` : '0m'}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Study Workload
          </span>
        </div>
      </section>

      {/* 4. Must Do Section */}
      {mustDoTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Flame size={14} color="var(--sarah-error)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-error)'
                }}
              >
                Must Do ({mustDoTasks.length})
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {mustDoTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* 5. Should Do Section */}
      {shouldDoTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <BookOpen size={14} color="var(--sarah-tertiary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-tertiary)'
                }}
              >
                Should Do ({shouldDoTasks.length})
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {shouldDoTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* 6. Later / Deferrable Section */}
      {laterTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                letterSpacing: '0.08em',
                textTransform: 'uppercase',
                color: 'var(--sarah-secondary)'
              }}
            >
              Later ({laterTasks.length})
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {laterTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* 7. Upcoming Reminders Section */}
      {upcomingRemindersPreview.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Bell size={13} color="var(--sarah-primary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.06em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-primary)'
                }}
              >
                Upcoming Reminders ({activeReminders.length})
              </span>
            </div>

            <button
              type="button"
              onClick={() => openCreateReminderModal()}
              style={{
                background: 'none',
                border: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '3px',
                fontSize: '11.5px',
                fontWeight: 600,
                color: 'var(--sarah-primary)',
                cursor: 'pointer',
                padding: '2px 4px'
              }}
            >
              <Plus size={13} />
              <span>Add Reminder</span>
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {upcomingRemindersPreview.map((reminder) => (
              <ReminderCard
                key={reminder.id}
                reminder={reminder}
                onDismiss={dismiss}
                onSnooze={snooze}
                onEdit={openEditReminderModal}
                onDelete={removeReminder}
              />
            ))}
          </div>
        </section>
      )}

      {/* 8. Pinned Academic Notes Section */}
      {previewPinnedNotes.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '4px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Pin size={13} fill="var(--sarah-tertiary)" color="var(--sarah-tertiary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.06em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-tertiary)'
                }}
              >
                Pinned Academic Notes ({pinnedNotes.length})
              </span>
            </div>

            {onNavigateToNotes && (
              <button
                type="button"
                onClick={onNavigateToNotes}
                style={{
                  background: 'none',
                  border: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '3px',
                  fontSize: '11.5px',
                  fontWeight: 600,
                  color: 'var(--sarah-primary)',
                  cursor: 'pointer',
                  padding: '2px 4px'
                }}
              >
                <span>View All ({notes.length})</span>
                <ArrowRight size={12} />
              </button>
            )}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {previewPinnedNotes.map((note) => (
              <NoteCard
                key={note.id}
                note={note}
                compact={true}
                onEdit={openEditNoteModal}
                onTogglePin={togglePin}
                onDelete={removeNote}
              />
            ))}
          </div>
        </section>
      )}

      {/* Empty State when zero total active tasks */}
      {activeTasks.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: '30px 10px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px'
          }}
        >
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: '20px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <CheckCircle2 size={30} />
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {completedTasks.length > 0 ? "You're all caught up." : "You're all clear."}
            </div>
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
              {completedTasks.length > 0 ? "All tasks are completed. Enjoy the breathing room." : "Start by adding your first task."}
            </div>
          </div>
          <button
            type="button"
            onClick={() => openCreateTaskModal()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'var(--sarah-primary)',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 16px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer',
              marginTop: '4px'
            }}
          >
            <Plus size={15} />
            <span>Add Task</span>
          </button>
        </div>
      )}
    </div>
  );
};
