import React, { useState } from 'react';
import {
  Bus,
  Check,
  ChevronDown,
  ChevronUp,
  Coffee,
  GraduationCap,
  Moon,
  Plus,
  TriangleAlert,
  UtensilsCrossed
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useNotes } from '../context/NotesContext';
import { useReminders } from '../context/RemindersContext';
import { useSubjects } from '../context/SubjectsContext';
import { useUserProfile } from '../context/UserProfileContext';
import { TaskCard } from '../components/TaskCard';
import { NoteCard } from '../components/NoteCard';
import { ReminderCard } from '../components/ReminderCard';
import { EmptyState, SectionHead } from '../components/ui';
import { formatMinutes, useNow } from '../lib/datetime';
import { formatClock, type PlanBlock, type PlanItem, type PlanStatus, type ScoredTask } from '../lib/planner';

interface TodayScreenProps {
  onNavigateToNotes?: () => void;
}

const STATUS_TAG: Record<PlanStatus, { label: string; tone: string }> = {
  clear: { label: 'All clear', tone: 'tag-ok' },
  optimal: { label: 'On track', tone: 'tag-ok' },
  tight: { label: 'Tight', tone: 'tag-warn' },
  overloaded: { label: 'Overloaded', tone: 'tag-danger' },
  rest_recommended: { label: 'Rest mode', tone: 'tag-accent' },
  past_bedtime: { label: 'Bedtime', tone: 'tag-accent' }
};

function urgencyColor(scored: ScoredTask): string {
  if (scored.urgency === 'overdue' || scored.urgency === 'today') return 'var(--danger)';
  if (scored.urgency === 'tomorrow') return 'var(--warn)';
  return 'var(--text-2)';
}

const BLOCK_ICON: Record<PlanBlock['kind'], React.ComponentType<{ size?: number }>> = {
  college: GraduationCap,
  commute: Bus,
  dinner: UtensilsCrossed,
  break: Coffee
};

const TIME_COL = 82;

export const TodayScreen: React.FC<TodayScreenProps> = ({ onNavigateToNotes }) => {
  const { completedTasks, toggleTaskCompletion, openEditTaskModal, openCreateTaskModal } = useTasks();
  const { pinnedNotes, notes, openEditNoteModal, togglePin } = useNotes();
  const { activeReminders, dismiss, snooze, openEditReminderModal, openCreateReminderModal } = useReminders();
  const { getSubjectColor } = useSubjects();
  const { profile, plan, studyGoal } = useUserProfile();
  const now = useNow(60000);

  const [showAllLater, setShowAllLater] = useState(false);
  const [showAllReminders, setShowAllReminders] = useState(false);

  const hour = new Date(now).getHours();
  const greeting = hour < 5 ? 'Still up' : hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
  const firstName = profile.name?.trim().split(' ')[0] || '';
  const dateLabel = new Date(now).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });

  const statusTag = STATUS_TAG[plan.status];
  const next = plan.next;
  const nextSession = next ? plan.items.find(i => i.kind === 'task' && i.task.id === next.task.id) : undefined;
  const hasSessions = plan.items.some(i => i.kind === 'task');
  const showPlan = hasSessions && plan.status !== 'past_bedtime';
  const atRiskIds = new Set(plan.atRisk.map(s => s.task.id));
  const later = plan.deferred.filter(s => !atRiskIds.has(s.task.id));
  const visibleLater = showAllLater ? later : later.slice(0, 4);
  const visibleReminders = showAllReminders ? activeReminders : activeReminders.slice(0, 3);
  const meterPercent = plan.atRisk.length > 0
    ? 100
    : plan.focusCapacityMinutes > 0 ? Math.min(100, Math.round((plan.plannedMinutes / plan.focusCapacityMinutes) * 100)) : 0;
  const meterColor = plan.atRisk.length > 0 ? 'var(--danger)' : plan.status === 'tight' ? 'var(--warn)' : 'var(--accent)';

  const renderTimelineRow = (item: PlanItem, index: number) => {
    if (item.kind !== 'task') {
      const Icon = BLOCK_ICON[item.kind];
      return (
        <li key={`${item.kind}-${index}`} className="list-row" style={{ minHeight: 40, padding: '6px 14px', color: 'var(--text-3)' }}>
          <span className="tnum" style={{ width: TIME_COL - 14, whiteSpace: 'nowrap', fontSize: 12, flexShrink: 0 }}>{formatClock(item.startMs)}</span>
          <Icon size={15} />
          <span style={{ fontSize: 13 }}>{item.label}</span>
          <span className="tnum" style={{ marginLeft: 'auto', fontSize: 12 }}>{formatMinutes(item.end - item.start)}</span>
        </li>
      );
    }

    const { task, scored } = item;
    return (
      <li key={`${task.id}-${item.part}`}>
        <div
          role="button"
          tabIndex={0}
          className="list-row"
          onClick={() => openEditTaskModal(task)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') openEditTaskModal(task);
          }}
          style={{ cursor: 'pointer' }}
        >
          <span className="tnum" style={{ width: TIME_COL - 14, whiteSpace: 'nowrap', fontSize: 12.5, fontWeight: 600, flexShrink: 0 }}>
            {formatClock(item.startMs)}
          </span>
          <span className="subject-mark" style={{ background: getSubjectColor(task.subject) }} />
          <div className="grow">
            <div className="row-title truncate">
              {task.title}
              {item.parts > 1 && <span className="meta" style={{ fontWeight: 500 }}>  part {item.part} of {item.parts}</span>}
            </div>
            <div className="meta truncate" style={{ color: scored.urgency === 'overdue' ? 'var(--danger)' : undefined }}>
              {scored.reason}
            </div>
          </div>
          <span className="meta tnum" style={{ fontSize: 12, flexShrink: 0 }}>{formatMinutes(item.end - item.start)}</span>
          <span className="check-hit">
            <button
              type="button"
              role="checkbox"
              aria-checked={false}
              aria-label={`Complete "${task.title}"`}
              className="check"
              onClick={(e) => {
                e.stopPropagation();
                toggleTaskCompletion(task.id);
              }}
            >
              <Check size={14} strokeWidth={3} />
            </button>
          </span>
        </div>
      </li>
    );
  };

  return (
    <div className="screen">
      <header>
        <h2 className="screen-title">{greeting}{firstName ? `, ${firstName}` : ''}</h2>
        <p className="screen-sub">
          {dateLabel}.{' '}
          {plan.minutesUntilBed > 0
            ? `${formatMinutes(plan.minutesUntilBed)} until bedtime at ${plan.bedtimeLabel}.`
            : `Bedtime was ${plan.bedtimeLabel}.`}
        </p>
      </header>

      {/* Tonight: one verdict, one meter, three numbers */}
      <section className="card stack-12" aria-labelledby="tonight-title" style={{ padding: 16 }}>
        <div className="row" style={{ justifyContent: 'space-between', gap: 12 }}>
          <h3 id="tonight-title" className="section-title">Tonight</h3>
          <span className={`tag ${statusTag.tone}`}>{statusTag.label}</span>
        </div>
        <div className="stack-4">
          <p style={{ fontSize: 17, fontWeight: 650, letterSpacing: '-0.015em', lineHeight: 1.3 }}>{plan.headline}</p>
          <p style={{ fontSize: 13.5, color: 'var(--text-2)', lineHeight: 1.5 }}>{plan.subtext}</p>
        </div>

        {plan.minutesUntilBed > 0 && plan.status !== 'clear' && (
          <div className="stack-8">
            <div
              className="meter"
              role="progressbar"
              aria-label="Planned focus against realistic capacity"
              aria-valuenow={meterPercent}
              aria-valuemin={0}
              aria-valuemax={100}
            >
              <span style={{ width: `${meterPercent}%`, background: meterColor }} />
            </div>
            <dl style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
              {[
                { label: 'Planned', value: formatMinutes(plan.plannedMinutes) },
                { label: 'Focus left', value: `~${formatMinutes(plan.focusCapacityMinutes)}` },
                { label: 'Free time', value: formatMinutes(plan.freeMinutes) }
              ].map(stat => (
                <div key={stat.label}>
                  <dt className="meta">{stat.label}</dt>
                  <dd className="tnum" style={{ fontSize: 15, fontWeight: 600 }}>{stat.value}</dd>
                </div>
              ))}
            </dl>
          </div>
        )}

        <div className="row" style={{ gap: 10, paddingTop: 12, borderTop: '1px solid var(--line)' }}>
          <span className="meta-strong" style={{ flexShrink: 0 }}>Study goal</span>
          <div
            className="meter grow"
            role="progressbar"
            aria-label="Daily study goal"
            aria-valuenow={studyGoal.percent}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <span style={{ width: `${studyGoal.percent}%`, background: studyGoal.isMet ? 'var(--ok)' : 'var(--accent)' }} />
          </div>
          <span className="tnum" style={{ fontSize: 12.5, fontWeight: 600, color: studyGoal.isMet ? 'var(--ok)' : 'var(--text)', flexShrink: 0 }}>
            {formatMinutes(studyGoal.completedMinutes)} / {formatMinutes(studyGoal.goalMinutes)}
          </span>
        </div>
      </section>

      {/* Next move: the one elevated, accent-edged surface on the screen */}
      {next ? (
        <section aria-labelledby="next-title" className="stack-8">
          <SectionHead id="next-title" title="Do this next" />
          <div
            role="button"
            tabIndex={0}
            onClick={() => openEditTaskModal(next.task)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') openEditTaskModal(next.task);
            }}
            className="card press stack-12"
            style={{ padding: 16, cursor: 'pointer', borderColor: 'rgba(var(--accent-rgb), 0.35)', boxShadow: 'var(--shadow-2)' }}
          >
            <div className="stack-4">
              <span style={{ fontSize: 13, fontWeight: 600, color: urgencyColor(next) }}>{next.reason}</span>
              <h4 style={{ fontSize: 19, fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.25 }}>{next.task.title}</h4>
              <span className="row meta" style={{ gap: 6 }}>
                <span className="swatch" style={{ background: getSubjectColor(next.task.subject) }} />
                {next.task.subject}
                {next.task.priority === 'must' && <span className="tag tag-danger" style={{ marginLeft: 4 }}>Must</span>}
              </span>
            </div>
            <div className="row" style={{ justifyContent: 'space-between', gap: 12 }}>
              <span className="meta tnum" style={{ fontSize: 12.5 }}>
                {nextSession
                  ? `Starts ${formatClock(nextSession.startMs)}, takes ${formatMinutes(next.task.estimatedMinutes)}`
                  : `Takes ${formatMinutes(next.task.estimatedMinutes)}, not scheduled tonight`}
              </span>
              <button
                type="button"
                className="btn btn-primary"
                onClick={(e) => {
                  e.stopPropagation();
                  toggleTaskCompletion(next.task.id);
                }}
              >
                <Check size={16} />
                Done
              </button>
            </div>
          </div>
        </section>
      ) : plan.ranked.length === 0 && (
        <EmptyState
          title={completedTasks.length > 0 ? 'All caught up' : 'Add what is on your plate'}
          body={completedTasks.length > 0
            ? 'Nothing open. Enjoy the evening.'
            : 'Add assignments with a deadline and a time estimate. Sarah fits them into your evening.'}
          action={(
            <button type="button" className="btn btn-primary" onClick={() => openCreateTaskModal()}>
              <Plus size={16} />
              Add task
            </button>
          )}
        />
      )}

      {/* Deadlines that cannot be finished before bed */}
      {plan.atRisk.length > 0 && plan.status !== 'past_bedtime' && (
        <section
          aria-labelledby="risk-title"
          className="stack-8"
          style={{ padding: 14, borderRadius: 'var(--r-surface)', background: 'var(--danger-soft)' }}
        >
          <h3 id="risk-title" className="row-8 section-title" style={{ color: 'var(--danger)' }}>
            <TriangleAlert size={16} />
            Won't fit before {plan.bedtimeLabel}
          </h3>
          <ul className="stack-4" style={{ listStyle: 'none' }}>
            {plan.atRisk.map(s => (
              <li key={s.task.id}>
                <button
                  type="button"
                  onClick={() => openEditTaskModal(s.task)}
                  className="row"
                  style={{ width: '100%', justifyContent: 'space-between', gap: 12, padding: '4px 0', textAlign: 'left' }}
                >
                  <span className="truncate" style={{ fontSize: 14, fontWeight: 600 }}>{s.task.title}</span>
                  <span style={{ fontSize: 12.5, color: 'var(--danger)', flexShrink: 0 }}>
                    {plan.plannedByTask[s.task.id]
                      ? `${formatMinutes(plan.plannedByTask[s.task.id])} of ${formatMinutes(s.task.estimatedMinutes)} fits`
                      : s.reason}
                  </span>
                </button>
              </li>
            ))}
          </ul>
          <p style={{ fontSize: 12.5, color: 'var(--text-2)', lineHeight: 1.5 }}>
            Trim an estimate or move a deadline, or ask for an extension tonight instead of tomorrow.
          </p>
        </section>
      )}

      {/* Tonight's timeline */}
      {showPlan && (
        <section aria-labelledby="plan-title" className="stack-8">
          <SectionHead
            id="plan-title"
            title="Tonight's plan"
            action={<span className="meta tnum">{formatMinutes(plan.plannedMinutes)}</span>}
          />
          <ol className="list" style={{ listStyle: 'none' }}>
            {plan.items.map(renderTimelineRow)}
            <li className="list-row" style={{ minHeight: 40, padding: '6px 14px', color: 'var(--accent-text)' }}>
              <span className="tnum" style={{ width: TIME_COL - 14, whiteSpace: 'nowrap', fontSize: 12, fontWeight: 600, flexShrink: 0 }}>{plan.bedtimeLabel}</span>
              <Moon size={15} />
              <span style={{ fontSize: 13, fontWeight: 600 }}>Bedtime</span>
            </li>
          </ol>
        </section>
      )}

      {/* Everything that can wait */}
      {later.length > 0 && (
        <section aria-labelledby="later-title" className="stack-8">
          <SectionHead
            id="later-title"
            title={plan.status === 'past_bedtime' ? 'Waiting for tomorrow' : 'Can wait'}
            count={later.length}
            action={later.length > 4 && (
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setShowAllLater(v => !v)}>
                {showAllLater ? 'Show less' : 'Show all'}
                {showAllLater ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
              </button>
            )}
          />
          <div className="list">
            {visibleLater.map(s => (
              <TaskCard key={s.task.id} task={s.task} reason={s.reason} onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />
            ))}
          </div>
        </section>
      )}

      {/* Reminders */}
      {activeReminders.length > 0 && (
        <section aria-labelledby="rem-title" className="stack-8">
          <SectionHead
            id="rem-title"
            title="Reminders"
            count={activeReminders.length}
            action={(
              <div className="row">
                {activeReminders.length > 3 && (
                  <button type="button" className="btn btn-ghost btn-sm" onClick={() => setShowAllReminders(v => !v)}>
                    {showAllReminders ? 'Show less' : 'Show all'}
                  </button>
                )}
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => openCreateReminderModal()}>
                  <Plus size={15} />
                  Add
                </button>
              </div>
            )}
          />
          <div className="list">
            {visibleReminders.map(r => (
              <ReminderCard key={r.id} reminder={r} onDismiss={dismiss} onSnooze={snooze} onEdit={openEditReminderModal} />
            ))}
          </div>
        </section>
      )}

      {/* Pinned notes */}
      {pinnedNotes.length > 0 && (
        <section aria-labelledby="pinned-title" className="stack-8">
          <SectionHead
            id="pinned-title"
            title="Pinned notes"
            count={pinnedNotes.length}
            action={onNavigateToNotes && (
              <button type="button" className="btn btn-ghost btn-sm" onClick={onNavigateToNotes}>
                All {notes.length}
              </button>
            )}
          />
          <div className="stack-8">
            {pinnedNotes.slice(0, 3).map(note => (
              <NoteCard key={note.id} note={note} compact onEdit={openEditNoteModal} onTogglePin={togglePin} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
