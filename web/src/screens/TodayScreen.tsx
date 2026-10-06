import React, { useState } from 'react';
import {
  Sparkles,
  Clock,
  CheckCircle2,
  Circle,
  Plus,
  Coffee,
  Check,
  Pin,
  ArrowRight,
  Bell,
  Moon,
  GraduationCap,
  Bus,
  UtensilsCrossed,
  TriangleAlert,
  CalendarClock,
  ChevronDown,
  ChevronUp,
  Target
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';
import { useSubjects } from '../context/SubjectsContext';
import { useUserProfile } from '../context/UserProfileContext';
import { TaskCard } from '../components/TaskCard';
import { NoteCard } from '../components/NoteCard';
import { ReminderCard } from '../components/ReminderCard';
import { formatMinutes, useNow } from '../lib/datetime';
import { formatClock, type PlanBlock, type PlanItem, type PlanStatus, type ScoredTask } from '../lib/planner';

interface TodayScreenProps {
  onNavigateToNotes?: () => void;
}

const sectionLabel = (color: string): React.CSSProperties => ({
  fontSize: '11px',
  fontWeight: 700,
  letterSpacing: '0.07em',
  textTransform: 'uppercase',
  color
});

const linkButton: React.CSSProperties = {
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
};

const STATUS_CHIP: Record<PlanStatus, { label: string; color: string; rgb: string }> = {
  clear: { label: 'ALL CLEAR', color: 'var(--sarah-success)', rgb: 'var(--sarah-success-rgb)' },
  optimal: { label: 'ON TRACK', color: 'var(--sarah-success)', rgb: 'var(--sarah-success-rgb)' },
  tight: { label: 'TIGHT', color: 'var(--sarah-tertiary)', rgb: 'var(--sarah-amber-rgb)' },
  overloaded: { label: 'OVERLOADED', color: 'var(--sarah-error)', rgb: 'var(--sarah-error-rgb)' },
  rest_recommended: { label: 'REST MODE', color: 'var(--sarah-violet)', rgb: 'var(--sarah-violet-rgb)' },
  past_bedtime: { label: 'BEDTIME', color: 'var(--sarah-violet)', rgb: 'var(--sarah-violet-rgb)' }
};

function urgencyBadge(scored: ScoredTask): { label: string; color: string; rgb: string } {
  switch (scored.urgency) {
    case 'overdue':
      return { label: 'OVERDUE', color: 'var(--sarah-error)', rgb: 'var(--sarah-error-rgb)' };
    case 'today':
      return { label: 'DUE TODAY', color: 'var(--sarah-error)', rgb: 'var(--sarah-error-rgb)' };
    case 'tomorrow':
      return { label: 'DUE TOMORROW', color: 'var(--sarah-tertiary)', rgb: 'var(--sarah-amber-rgb)' };
    default:
      return scored.task.priority === 'must'
        ? { label: 'MUST DO', color: 'var(--sarah-error)', rgb: 'var(--sarah-error-rgb)' }
        : { label: 'UP NEXT', color: 'var(--sarah-primary)', rgb: 'var(--sarah-primary-rgb)' };
  }
}

const BLOCK_ICON: Record<PlanBlock['kind'], React.ComponentType<{ size?: number }>> = {
  college: GraduationCap,
  commute: Bus,
  dinner: UtensilsCrossed,
  break: Coffee
};

const Chip: React.FC<{ label: string; color: string; rgb: string }> = ({ label, color, rgb }) => (
  <span
    style={{
      fontSize: '10.5px',
      fontWeight: 700,
      padding: '3px 8px',
      borderRadius: '8px',
      backgroundColor: `rgba(${rgb}, 0.12)`,
      color,
      whiteSpace: 'nowrap'
    }}
  >
    {label}
  </span>
);

export const TodayScreen: React.FC<TodayScreenProps> = ({ onNavigateToNotes }) => {
  const { completedTasks, toggleTaskCompletion, openEditTaskModal, openCreateTaskModal } = useTasks();
  const { pinnedNotes, notes, openEditNoteModal, togglePin, removeNote } = useNotes();
  const {
    activeReminders,
    dismiss,
    snooze,
    openEditReminderModal,
    openCreateReminderModal,
    removeReminder
  } = useReminders();
  const { getSubjectColor } = useSubjects();
  const { profile, plan, studyGoal } = useUserProfile();
  const now = useNow(60000);

  const [showAllLater, setShowAllLater] = useState(false);
  const [showAllReminders, setShowAllReminders] = useState(false);

  const hour = new Date(now).getHours();
  const greeting = hour < 5 ? 'Still up' : hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
  const firstName = profile.name?.trim().split(' ')[0] || '';
  const dateLabel = new Date(now).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });

  const chip = STATUS_CHIP[plan.status];
  const next = plan.next;
  const nextSessions = next
    ? plan.items.filter(i => i.kind === 'task' && i.task.id === next.task.id)
    : [];
  const hasSessions = plan.items.some(i => i.kind === 'task');
  const atRiskIds = new Set(plan.atRisk.map(s => s.task.id));
  const later = plan.deferred.filter(s => !atRiskIds.has(s.task.id));
  const visibleLater = showAllLater ? later : later.slice(0, 3);
  const visibleReminders = showAllReminders ? activeReminders : activeReminders.slice(0, 3);
  const meterPercent = plan.focusCapacityMinutes > 0
    ? Math.min(100, Math.round((plan.plannedMinutes / plan.focusCapacityMinutes) * 100))
    : 0;

  const renderTimelineRow = (item: PlanItem, index: number) => {
    const time = formatClock(item.startMs);
    if (item.kind !== 'task') {
      const Icon = BLOCK_ICON[item.kind];
      return (
        <li key={`${item.kind}-${index}`} style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '5px 0' }}>
          <span style={{ width: '58px', flexShrink: 0, fontSize: '11px', color: 'var(--sarah-outline)', fontVariantNumeric: 'tabular-nums' }}>
            {time}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--sarah-secondary)' }}>
            <Icon size={13} />
            {item.label} · {formatMinutes(item.end - item.start)}
          </span>
        </li>
      );
    }

    const { task, scored } = item;
    return (
      <li key={`${task.id}-${item.part}`} style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', padding: '6px 0' }}>
        <span style={{ width: '58px', flexShrink: 0, paddingTop: '3px', fontSize: '12px', fontWeight: 700, color: 'var(--sarah-on-background)', fontVariantNumeric: 'tabular-nums' }}>
          {time}
        </span>
        <div
          role="button"
          tabIndex={0}
          onClick={() => openEditTaskModal(task)}
          onKeyDown={(e) => { if (e.key === 'Enter') openEditTaskModal(task); }}
          className="surface-card btn-press"
          style={{
            flex: 1,
            minWidth: 0,
            padding: '9px 12px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            cursor: 'pointer',
            borderLeft: `3px solid ${getSubjectColor(task.subject)}`
          }}
        >
          <button
            type="button"
            aria-label={`Mark "${task.title}" as done`}
            onClick={(e) => {
              e.stopPropagation();
              toggleTaskCompletion(task.id);
            }}
            style={{ background: 'none', border: 'none', padding: '4px', margin: '-4px', cursor: 'pointer', color: 'var(--sarah-outline)', display: 'flex', flexShrink: 0 }}
          >
            <Circle size={20} strokeWidth={1.8} />
          </button>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '13.5px', fontWeight: 600, color: 'var(--sarah-on-background)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {task.title}
              {item.parts > 1 && (
                <span style={{ fontWeight: 500, color: 'var(--sarah-secondary)' }}> · part {item.part}/{item.parts}</span>
              )}
            </div>
            <div style={{ fontSize: '11px', color: scored.urgency === 'overdue' ? 'var(--sarah-error)' : 'var(--sarah-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {task.subject} · {scored.reason}
            </div>
          </div>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)', flexShrink: 0 }}>
            {formatMinutes(item.end - item.start)}
          </span>
        </div>
      </li>
    );
  };

  return (
    <div
      className="animate-fade-in"
      style={{ padding: '16px 18px 96px 18px', display: 'flex', flexDirection: 'column', gap: '16px' }}
    >
      {/* 1. Greeting */}
      <section style={{ paddingTop: '2px' }}>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--sarah-on-background)', letterSpacing: '-0.03em', margin: 0 }}>
          {greeting}{firstName ? `, ${firstName}` : ''}
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--sarah-on-surface-variant)', margin: 0 }}>
          {dateLabel} · {plan.minutesUntilBed > 0
            ? `${formatMinutes(plan.minutesUntilBed)} until bedtime (${plan.bedtimeLabel})`
            : `Bedtime was ${plan.bedtimeLabel}`}
        </p>
      </section>

      {/* 2. Tonight verdict + capacity meter */}
      <section
        className="glass-card"
        aria-live="polite"
        style={{
          padding: '16px 18px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
          backgroundColor: plan.status === 'overloaded' ? 'var(--sarah-error-surface)' : 'var(--glass-bg-strong)',
          border: plan.status === 'overloaded'
            ? '1px solid rgba(var(--sarah-error-rgb), 0.25)'
            : '1px solid var(--glass-border)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Sparkles size={15} color="var(--sarah-primary)" />
            <span style={sectionLabel('var(--sarah-primary)')}>Tonight</span>
          </div>
          <Chip {...chip} />
        </div>

        <div>
          <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--sarah-on-background)', margin: '0 0 3px 0', letterSpacing: '-0.01em' }}>
            {plan.headline}
          </h3>
          <p style={{ fontSize: '12.5px', color: 'var(--sarah-on-surface-variant)', margin: 0, lineHeight: 1.45 }}>
            {plan.subtext}
          </p>
        </div>

        {plan.minutesUntilBed > 0 && plan.status !== 'clear' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <div
              role="progressbar"
              aria-label="Planned focus against realistic capacity"
              aria-valuenow={meterPercent}
              aria-valuemin={0}
              aria-valuemax={100}
              style={{ height: '7px', borderRadius: '99px', backgroundColor: 'var(--sarah-surface-container-high)', overflow: 'hidden' }}
            >
              <div
                style={{
                  width: `${plan.atRisk.length > 0 ? 100 : meterPercent}%`,
                  height: '100%',
                  borderRadius: '99px',
                  backgroundColor: plan.atRisk.length > 0
                    ? 'var(--sarah-error)'
                    : plan.status === 'tight' ? 'var(--sarah-amber)' : 'var(--sarah-primary)',
                  transition: 'width 0.35s cubic-bezier(0.16, 1, 0.3, 1)'
                }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
              <span>
                <strong style={{ color: 'var(--sarah-on-background)' }}>{formatMinutes(plan.plannedMinutes)}</strong> planned
              </span>
              <span>
                ~{formatMinutes(plan.focusCapacityMinutes)} focus · {formatMinutes(plan.freeMinutes)} free
              </span>
            </div>
          </div>
        )}

        {/* Daily study goal, folded into the same card */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', paddingTop: '10px', borderTop: '1px solid var(--sarah-outline-variant)' }}>
          <Target size={13} color={studyGoal.isMet ? 'var(--sarah-success)' : 'var(--sarah-secondary)'} />
          <span style={{ fontSize: '11.5px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>Study goal</span>
          <div
            role="progressbar"
            aria-label="Daily study goal progress"
            aria-valuenow={studyGoal.percent}
            aria-valuemin={0}
            aria-valuemax={100}
            style={{ flex: 1, height: '5px', borderRadius: '99px', backgroundColor: 'var(--sarah-surface-container-high)', overflow: 'hidden' }}
          >
            <div style={{ width: `${studyGoal.percent}%`, height: '100%', backgroundColor: studyGoal.isMet ? 'var(--sarah-success)' : 'var(--sarah-primary)', transition: 'width 0.35s ease' }} />
          </div>
          <span style={{ fontSize: '11.5px', fontWeight: 700, color: studyGoal.isMet ? 'var(--sarah-success)' : 'var(--sarah-on-background)' }}>
            {formatMinutes(studyGoal.completedMinutes)} / {formatMinutes(studyGoal.goalMinutes)}{studyGoal.isMet ? ' ✓' : ''}
          </span>
        </div>
      </section>

      {/* 3. Next move */}
      {next ? (
        <section
          className="glass-card btn-press"
          onClick={() => openEditTaskModal(next.task)}
          style={{ padding: '18px 20px', display: 'flex', flexDirection: 'column', gap: '12px', position: 'relative', overflow: 'clip', cursor: 'pointer' }}
        >
          <div
            style={{
              position: 'absolute',
              top: '-25px',
              right: '-25px',
              width: '130px',
              height: '130px',
              borderRadius: '50%',
              background: 'radial-gradient(circle, rgba(var(--sarah-primary-container-rgb), 0.22) 0%, transparent 70%)',
              pointerEvents: 'none'
            }}
          />
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '7px' }}>
              <ArrowRight size={16} color="var(--sarah-primary)" />
              <span style={sectionLabel('var(--sarah-primary)')}>Do this next</span>
            </div>
            <Chip {...urgencyBadge(next)} />
          </div>

          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--sarah-on-background)', letterSpacing: '-0.02em', marginBottom: '4px' }}>
              {next.task.title}
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', minWidth: 0 }}>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: getSubjectColor(next.task.subject), flexShrink: 0 }} />
              <span style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', fontWeight: 500, whiteSpace: 'nowrap' }}>
                {next.task.subject}
              </span>
              <span style={{ fontSize: '10px', color: 'var(--sarah-outline)' }}>•</span>
              <span style={{ fontSize: '12px', color: next.urgency === 'overdue' ? 'var(--sarah-error)' : 'var(--sarah-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {next.reason}
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '10px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--sarah-secondary)', fontSize: '12px', fontWeight: 500, minWidth: 0 }}>
              <Clock size={14} style={{ flexShrink: 0 }} />
              <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {nextSessions.length > 0 && nextSessions[0].kind === 'task'
                  ? `${formatClock(nextSessions[0].startMs)} · ${formatMinutes(next.task.estimatedMinutes)}`
                  : `${formatMinutes(next.task.estimatedMinutes)} · not scheduled tonight`}
              </span>
            </div>
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                toggleTaskCompletion(next.task.id);
              }}
              className="btn-press"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                background: 'var(--sarah-primary)',
                color: 'var(--sarah-on-primary)',
                border: 'none',
                borderRadius: '12px',
                padding: '8px 16px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(var(--sarah-primary-rgb), 0.3)',
                flexShrink: 0
              }}
            >
              <Check size={14} strokeWidth={2.5} />
              <span>Mark done</span>
            </button>
          </div>
        </section>
      ) : plan.ranked.length === 0 && (
        <section
          className="glass-card"
          style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px', backgroundColor: 'var(--glass-bg)' }}
        >
          <div
            style={{
              width: '46px',
              height: '46px',
              borderRadius: '14px',
              backgroundColor: completedTasks.length > 0 ? 'rgba(var(--sarah-success-rgb), 0.12)' : 'rgba(var(--sarah-primary-rgb), 0.1)',
              color: completedTasks.length > 0 ? 'var(--sarah-success)' : 'var(--sarah-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}
          >
            {completedTasks.length > 0 ? <Coffee size={24} /> : <CheckCircle2 size={24} />}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '15.5px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {completedTasks.length > 0 ? 'All caught up' : 'Add what’s on your plate'}
            </div>
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-on-surface-variant)', marginTop: '2px' }}>
              {completedTasks.length > 0
                ? 'Nothing open. Enjoy the breathing room.'
                : 'Add assignments with a deadline and a time estimate — Sarah plans your evening around them.'}
            </div>
          </div>
          <button
            type="button"
            onClick={() => openCreateTaskModal()}
            className="btn-press"
            aria-label="Add a task"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              background: 'var(--sarah-primary)',
              color: 'var(--sarah-on-primary)',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 12px',
              fontSize: '12.5px',
              fontWeight: 600,
              cursor: 'pointer',
              flexShrink: 0
            }}
          >
            <Plus size={14} />
            <span>Add</span>
          </button>
        </section>
      )}

      {/* 4. Deadlines that will not fit */}
      {plan.atRisk.length > 0 && plan.status !== 'past_bedtime' && (
        <section
          className="surface-card"
          style={{ padding: '12px 14px', display: 'flex', flexDirection: 'column', gap: '8px', backgroundColor: 'var(--sarah-error-surface)', border: '1px solid rgba(var(--sarah-error-rgb), 0.22)' }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <TriangleAlert size={14} color="var(--sarah-error)" />
            <span style={sectionLabel('var(--sarah-error)')}>Won’t fit before {plan.bedtimeLabel}</span>
          </div>
          {plan.atRisk.map(s => (
            <button
              key={s.task.id}
              type="button"
              onClick={() => openEditTaskModal(s.task)}
              style={{ display: 'flex', justifyContent: 'space-between', gap: '8px', background: 'none', border: 'none', padding: 0, cursor: 'pointer', textAlign: 'left', color: 'var(--sarah-on-background)' }}
            >
              <span style={{ fontSize: '13px', fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{s.task.title}</span>
              <span style={{ fontSize: '11.5px', color: 'var(--sarah-error)', flexShrink: 0 }}>
                {plan.plannedByTask[s.task.id]
                  ? `only ${formatMinutes(plan.plannedByTask[s.task.id])} of ${formatMinutes(s.task.estimatedMinutes)} fits`
                  : s.reason}
              </span>
            </button>
          ))}
          <p style={{ fontSize: '11.5px', color: 'var(--sarah-on-surface-variant)', margin: 0, lineHeight: 1.45 }}>
            Tap one to trim its estimate or move the deadline — or email your professor tonight rather than tomorrow.
          </p>
        </section>
      )}

      {/* 5. Tonight's timeline */}
      {hasSessions && plan.status !== 'past_bedtime' && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CalendarClock size={14} color="var(--sarah-primary)" />
              <span style={sectionLabel('var(--sarah-primary)')}>Tonight’s plan</span>
            </div>
            <span style={{ fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 600 }}>
              {plan.planned.length} task{plan.planned.length === 1 ? '' : 's'} · {formatMinutes(plan.plannedMinutes)}
            </span>
          </div>
          <ol style={{ listStyle: 'none', margin: 0, padding: 0 }}>
            {plan.items.map(renderTimelineRow)}
            <li style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '5px 0' }}>
              <span style={{ width: '58px', flexShrink: 0, fontSize: '11px', color: 'var(--sarah-violet)', fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>
                {plan.bedtimeLabel}
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--sarah-violet)', fontWeight: 600 }}>
                <Moon size={13} /> Bedtime
              </span>
            </li>
          </ol>
        </section>
      )}

      {/* 6. Everything that can wait */}
      {later.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <span style={sectionLabel('var(--sarah-secondary)')}>
              {plan.status === 'past_bedtime' ? 'Waiting for tomorrow' : 'Can wait'} ({later.length})
            </span>
            {later.length > 3 && (
              <button type="button" onClick={() => setShowAllLater(v => !v)} style={linkButton}>
                <span>{showAllLater ? 'Show less' : `Show all ${later.length}`}</span>
                {showAllLater ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
              </button>
            )}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {visibleLater.map(s => (
              <TaskCard key={s.task.id} task={s.task} showDate onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />
            ))}
          </div>
        </section>
      )}

      {/* 7. Reminders */}
      {activeReminders.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '2px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Bell size={13} color="var(--sarah-primary)" />
              <span style={sectionLabel('var(--sarah-primary)')}>Reminders ({activeReminders.length})</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              {activeReminders.length > 3 && (
                <button type="button" onClick={() => setShowAllReminders(v => !v)} style={linkButton}>
                  {showAllReminders ? 'Show less' : 'Show all'}
                </button>
              )}
              <button type="button" onClick={() => openCreateReminderModal()} style={linkButton}>
                <Plus size={13} />
                <span>Add</span>
              </button>
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {visibleReminders.map((reminder) => (
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

      {/* 8. Pinned notes */}
      {pinnedNotes.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '2px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Pin size={13} fill="var(--sarah-tertiary)" color="var(--sarah-tertiary)" />
              <span style={sectionLabel('var(--sarah-tertiary)')}>Pinned notes ({pinnedNotes.length})</span>
            </div>
            {onNavigateToNotes && (
              <button type="button" onClick={onNavigateToNotes} style={linkButton}>
                <span>All notes ({notes.length})</span>
                <ArrowRight size={12} />
              </button>
            )}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {pinnedNotes.slice(0, 3).map((note) => (
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
    </div>
  );
};
