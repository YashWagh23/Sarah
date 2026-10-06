import type { EnergyLevel, Task, UserProfile } from './db';
import { formatMinutes, parseTimeToMinutes, toLocalDateStr } from './datetime';

/**
 * Tonight Planner
 *
 * Answers Sarah's core question — "given my deadlines and the time and energy I
 * actually have, what should I do next?" — by scoring every open task and laying
 * real focus sessions onto the clock between now and bedtime. It is a pure
 * function of (tasks, profile, now) so the UI, the tests and a long-open PWA tab
 * all agree on the same plan.
 *
 * Times are expressed on a "night axis": minutes since the local midnight that
 * started this evening. A bedtime after midnight (e.g. 00:30) sits past 1440 on
 * that axis, and opening the app at 00:10 still belongs to the previous evening.
 */

export interface EnergyProfile {
  label: string;
  /** Share of free time that realistically turns into focused work. */
  focusShare: number;
  /** Longest single focus block before a break. */
  blockMinutes: number;
  breakMinutes: number;
  hint: string;
}

export const ENERGY_PROFILES: Record<EnergyLevel, EnergyProfile> = {
  high: { label: 'High', focusShare: 0.9, blockMinutes: 60, breakMinutes: 10, hint: '60m focus blocks' },
  normal: { label: 'Steady', focusShare: 0.8, blockMinutes: 45, breakMinutes: 10, hint: '45m focus blocks' },
  low: { label: 'Low', focusShare: 0.6, blockMinutes: 30, breakMinutes: 10, hint: '30m blocks, more breaks' },
  exhausted: { label: 'Rest', focusShare: 0.35, blockMinutes: 25, breakMinutes: 15, hint: 'Only what can’t wait' }
};

export type Urgency = 'overdue' | 'today' | 'tomorrow' | 'soon' | 'this_week' | 'later';

export interface ScoredTask {
  task: Task;
  score: number;
  urgency: Urgency;
  dueAtMs: number;
  /** Has to be done before sleeping: overdue, due today, or due tomorrow morning. */
  needsTonight: boolean;
  reason: string;
}

export interface PlanSession {
  kind: 'task';
  task: Task;
  scored: ScoredTask;
  start: number;
  end: number;
  startMs: number;
  endMs: number;
  /** 1-based part number when a task is split across several focus blocks. */
  part: number;
  parts: number;
}

export interface PlanBlock {
  kind: 'college' | 'commute' | 'dinner' | 'break';
  label: string;
  start: number;
  end: number;
  startMs: number;
  endMs: number;
}

export type PlanItem = PlanSession | PlanBlock;

export type PlanStatus = 'clear' | 'optimal' | 'tight' | 'overloaded' | 'rest_recommended' | 'past_bedtime';

export interface TonightPlan {
  status: PlanStatus;
  headline: string;
  subtext: string;
  bedtimeLabel: string;
  bedtimeMs: number;
  minutesUntilBed: number;
  /** Minutes before bedtime not taken by college, commute or dinner. */
  freeMinutes: number;
  /** Focus minutes the current energy level can realistically sustain tonight. */
  focusCapacityMinutes: number;
  plannedMinutes: number;
  items: PlanItem[];
  /** Open tasks in the order Sarah would tackle them. */
  ranked: ScoredTask[];
  /** Tasks with at least one session tonight, in plan order. */
  planned: ScoredTask[];
  /** Open tasks with no session tonight. */
  deferred: ScoredTask[];
  /** Tonight-critical tasks that do not fully fit before bedtime. */
  atRisk: ScoredTask[];
  shortfallMinutes: number;
  /** Focus minutes scheduled tonight per task id (less than the estimate when it only partly fits). */
  plannedByTask: Record<string, number>;
  next: ScoredTask | null;
  isCollegeDay: boolean;
}

const DAY_MINUTES = 24 * 60;
const DINNER_START = 20 * 60;
const DINNER_MINUTES = 30;
const MIN_SESSION = 10;
/** Shortest slice worth starting when it will not finish the task. */
const MIN_PARTIAL = 20;

const URGENCY_WEIGHT: Record<Urgency, number> = {
  overdue: 80,
  today: 60,
  tomorrow: 42,
  soon: 26,
  this_week: 12,
  later: 0
};

const PRIORITY_WEIGHT: Record<Task['priority'], number> = {
  must: 40,
  should: 20,
  later: 0
};

export function taskDueMs(task: Task): number {
  const [y, m, d] = task.deadline.split('-').map(Number);
  const [hh, mm] = (task.deadlineTime || '23:59').split(':').map(Number);
  return new Date(y, (m || 1) - 1, d || 1, hh || 0, mm || 0, 0, 0).getTime();
}

function dayDiff(fromDateStr: string, toDateStr: string): number {
  const [fy, fm, fd] = fromDateStr.split('-').map(Number);
  const [ty, tm, td] = toDateStr.split('-').map(Number);
  // UTC keeps a daylight-saving shift from turning a whole day into 23 hours.
  return Math.round((Date.UTC(ty, tm - 1, td) - Date.UTC(fy, fm - 1, fd)) / 86400000);
}

export function formatClock(ms: number): string {
  return new Date(ms).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

function formatDay(ms: number): string {
  return new Date(ms).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
}

export function scoreTask(task: Task, nowMs: number): ScoredTask {
  const dueAtMs = taskDueMs(task);
  const days = dayDiff(toLocalDateStr(new Date(nowMs)), task.deadline);
  const timeLabel = task.deadlineTime && task.deadlineTime !== '23:59' ? ` at ${formatClock(dueAtMs)}` : '';

  let urgency: Urgency;
  let reason: string;
  if (dueAtMs < nowMs) {
    urgency = 'overdue';
    const lateDays = Math.max(0, -days);
    reason = lateDays === 0 ? `Was due today${timeLabel}` : lateDays === 1 ? 'Overdue since yesterday' : `Overdue by ${lateDays} days`;
  } else if (days <= 0) {
    urgency = 'today';
    reason = `Due today${timeLabel || ' by midnight'}`;
  } else if (days === 1) {
    urgency = 'tomorrow';
    reason = `Due tomorrow${timeLabel}`;
  } else if (days <= 3) {
    urgency = 'soon';
    reason = `Due in ${days} days`;
  } else if (days <= 7) {
    urgency = 'this_week';
    reason = `Due ${formatDay(dueAtMs)}`;
  } else {
    urgency = 'later';
    reason = `Due ${formatDay(dueAtMs)}`;
  }

  const minutes = task.estimatedMinutes || 30;
  const sizeWeight = minutes <= 25 ? 8 : minutes <= 45 ? 4 : minutes > 90 ? -6 : 0;
  const score = URGENCY_WEIGHT[urgency] + PRIORITY_WEIGHT[task.priority] + sizeWeight;

  // Something due before noon tomorrow can only realistically happen tonight.
  const dueTomorrowMorning = urgency === 'tomorrow' && new Date(dueAtMs).getHours() < 12;
  const needsTonight = urgency === 'overdue' || urgency === 'today' || dueTomorrowMorning;

  return { task, score, urgency, dueAtMs, needsTonight, reason };
}

export function rankTasks(tasks: Task[], nowMs: number): ScoredTask[] {
  return tasks
    .filter(t => !t.completed)
    .map(t => scoreTask(t, nowMs))
    .sort((a, b) =>
      Number(b.needsTonight) - Number(a.needsTonight) ||
      b.score - a.score ||
      a.dueAtMs - b.dueAtMs ||
      (a.task.estimatedMinutes || 30) - (b.task.estimatedMinutes || 30)
    );
}

type ScheduleProfile = Pick<
  UserProfile,
  'targetBedtime' | 'wakeUpTime' | 'collegeEndTime' | 'commuteMinutes' | 'energyLevel' | 'collegeDays'
>;

export function buildTonightPlan(tasks: Task[], profile: ScheduleProfile, nowMs: number): TonightPlan {
  const now = new Date(nowMs);
  const nowRaw = now.getHours() * 60 + now.getMinutes();
  const wake = parseTimeToMinutes(profile.wakeUpTime, 7 * 60);
  const bedRaw = parseTimeToMinutes(profile.targetBedtime, 23 * 60 + 30);

  // A bedtime at or before the wake-up time is after midnight.
  const bed = bedRaw <= wake ? bedRaw + DAY_MINUTES : bedRaw;
  // Small hours before waking up still belong to last night.
  const isSmallHours = nowRaw < wake;
  const nowAxis = isSmallHours ? nowRaw + DAY_MINUTES : nowRaw;

  const evening = new Date(now);
  if (isSmallHours) evening.setDate(evening.getDate() - 1);
  evening.setHours(0, 0, 0, 0);
  const axisToMs = (axis: number) => evening.getTime() + axis * 60000;

  const energy = ENERGY_PROFILES[profile.energyLevel] ?? ENERGY_PROFILES.normal;
  const collegeDays = Array.isArray(profile.collegeDays) ? profile.collegeDays : [1, 2, 3, 4, 5];
  const isCollegeDay = collegeDays.includes(evening.getDay());

  const bedtimeMs = axisToMs(bed);
  const bedtimeLabel = formatClock(bedtimeMs);
  const minutesUntilBed = Math.max(0, bed - nowAxis);

  // ── Blocked time between now and bed ──────────────────────────────────────
  const blocks: PlanBlock[] = [];
  const addBlock = (kind: PlanBlock['kind'], label: string, start: number, end: number) => {
    const s = Math.max(start, nowAxis);
    const e = Math.min(end, bed);
    if (e > s) blocks.push({ kind, label, start: s, end: e, startMs: axisToMs(s), endMs: axisToMs(e) });
  };

  if (isCollegeDay && !isSmallHours) {
    const collegeEnd = parseTimeToMinutes(profile.collegeEndTime, 17 * 60);
    const commute = Math.max(0, profile.commuteMinutes || 0);
    addBlock('college', 'Classes', nowAxis, collegeEnd);
    addBlock('commute', 'Commute home', Math.max(nowAxis, collegeEnd), collegeEnd + commute);
  }
  if (!isSmallHours && nowAxis < DINNER_START) {
    addBlock('dinner', 'Dinner', DINNER_START, DINNER_START + DINNER_MINUTES);
  }
  blocks.sort((a, b) => a.start - b.start);
  // Overlaps (e.g. a late commute running into dinner) collapse into the earlier block.
  for (let i = 1; i < blocks.length; i++) {
    if (blocks[i].start < blocks[i - 1].end) {
      blocks[i] = { ...blocks[i], start: blocks[i - 1].end, startMs: blocks[i - 1].endMs };
    }
  }
  const realBlocks = blocks.filter(b => b.end > b.start);

  const blockedMinutes = realBlocks.reduce((acc, b) => acc + (b.end - b.start), 0);
  const freeMinutes = Math.max(0, minutesUntilBed - blockedMinutes);
  const focusCapacityMinutes = Math.round(freeMinutes * energy.focusShare);

  // ── Rank and lay sessions onto the clock ─────────────────────────────────
  const ranked = rankTasks(tasks, nowMs);
  const restMode = profile.energyLevel === 'exhausted';
  const queue = restMode ? ranked.filter(s => s.needsTonight || s.urgency === 'tomorrow') : ranked;

  const remaining = new Map(queue.map(s => [s.task.id, s.task.estimatedMinutes || 30]));
  const sessions: PlanSession[] = [];
  const items: PlanItem[] = [];
  let clock = nowAxis;
  let focusUsed = 0;
  let blockIndex = 0;

  while (clock < bed) {
    const block = realBlocks[blockIndex];
    if (block && block.start <= clock) {
      items.push(block);
      clock = Math.max(clock, block.end);
      blockIndex++;
      continue;
    }

    const limit = Math.min(block ? block.start : bed, bed);
    const window = limit - clock;
    const current = queue.find(s => (remaining.get(s.task.id) ?? 0) > 0);
    const budget = focusCapacityMinutes - focusUsed;
    if (!current || budget < MIN_SESSION) break;
    if (window < MIN_SESSION) {
      clock = limit;
      continue;
    }

    const left = remaining.get(current.task.id) ?? 0;
    const chunk = Math.min(left, energy.blockMinutes, window, budget);
    // A 10-minute sliver of a big task right before dinner is not a real session.
    if (chunk < left && chunk < MIN_PARTIAL) {
      if (window <= budget) {
        clock = limit;
        continue;
      }
      break;
    }
    const session: PlanSession = {
      kind: 'task',
      task: current.task,
      scored: current,
      start: clock,
      end: clock + chunk,
      startMs: axisToMs(clock),
      endMs: axisToMs(clock + chunk),
      part: 1,
      parts: 1
    };
    sessions.push(session);
    items.push(session);
    remaining.set(current.task.id, left - chunk);
    focusUsed += chunk;
    clock += chunk;

    const moreWork = queue.some(s => (remaining.get(s.task.id) ?? 0) > 0);
    if (moreWork && limit - clock >= energy.breakMinutes + MIN_SESSION) {
      const brk: PlanBlock = {
        kind: 'break',
        label: 'Break',
        start: clock,
        end: clock + energy.breakMinutes,
        startMs: axisToMs(clock),
        endMs: axisToMs(clock + energy.breakMinutes)
      };
      items.push(brk);
      clock = brk.end;
    }
  }

  // Keep fixed parts of the evening visible even when there is no work left.
  for (; blockIndex < realBlocks.length; blockIndex++) items.push(realBlocks[blockIndex]);
  // A trailing break with nothing after it is noise.
  while (items.length > 0 && items[items.length - 1].kind === 'break') items.pop();

  // Number the parts of tasks split across several blocks.
  const partsById = new Map<string, number>();
  for (const s of sessions) partsById.set(s.task.id, (partsById.get(s.task.id) ?? 0) + 1);
  const seen = new Map<string, number>();
  for (const s of sessions) {
    const n = (seen.get(s.task.id) ?? 0) + 1;
    seen.set(s.task.id, n);
    s.part = n;
    s.parts = partsById.get(s.task.id) ?? 1;
  }

  const plannedByTask: Record<string, number> = {};
  for (const s of sessions) plannedByTask[s.task.id] = (plannedByTask[s.task.id] ?? 0) + (s.end - s.start);
  const plannedIds = new Set(sessions.map(s => s.task.id));
  const planned = ranked.filter(s => plannedIds.has(s.task.id));
  // Keep plan order (the order sessions start), not raw rank order.
  planned.sort((a, b) =>
    sessions.findIndex(s => s.task.id === a.task.id) - sessions.findIndex(s => s.task.id === b.task.id));
  const deferred = ranked.filter(s => !plannedIds.has(s.task.id));
  const atRisk = ranked.filter(s => s.needsTonight && (remaining.get(s.task.id) ?? s.task.estimatedMinutes ?? 30) > 0);
  const shortfallMinutes = atRisk.reduce((acc, s) => acc + (remaining.get(s.task.id) ?? s.task.estimatedMinutes ?? 30), 0);
  const plannedMinutes = focusUsed;
  const pastBedtime = minutesUntilBed <= 0;
  const next = planned[0] ?? (pastBedtime ? null : ranked[0] ?? null);

  // ── Verdict ──────────────────────────────────────────────────────────────
  let status: PlanStatus;
  let headline: string;
  let subtext: string;
  const plural = (n: number, word: string) => `${n} ${word}${n === 1 ? '' : 's'}`;

  if (ranked.length === 0) {
    status = 'clear';
    headline = 'Nothing due — your evening is yours';
    subtext = pastBedtime
      ? 'Get some sleep. Add anything new when it comes up.'
      : `${formatMinutes(freeMinutes)} free before ${bedtimeLabel}.`;
  } else if (pastBedtime) {
    status = 'past_bedtime';
    headline = 'It’s past your bedtime';
    subtext = `Sleep beats a late-night cram. ${plural(ranked.length, 'task')} will be waiting in the morning.`;
  } else if (atRisk.length > 0) {
    status = 'overloaded';
    headline = atRisk.length === 1
      ? 'One deadline won’t fit tonight'
      : `${atRisk.length} deadlines won’t fit tonight`;
    subtext = `About ${formatMinutes(shortfallMinutes)} short before ${bedtimeLabel}. Start with the top item, trim scope, or ask for an extension early.`;
  } else if (restMode) {
    status = 'rest_recommended';
    headline = 'Rest mode — only what can’t wait';
    subtext = planned.length > 0
      ? `${plural(planned.length, 'urgent task')} planned; everything else moved to later.`
      : `Nothing urgent tonight. ${plural(deferred.length, 'task')} moved to later.`;
  } else if (
    plannedMinutes >= focusCapacityMinutes * 0.85 ||
    deferred.some(s => s.urgency === 'tomorrow' || s.urgency === 'soon')
  ) {
    status = 'tight';
    headline = 'Tight, but tonight’s deadlines fit';
    subtext = `${formatMinutes(plannedMinutes)} planned of ~${formatMinutes(focusCapacityMinutes)} realistic focus before ${bedtimeLabel}.`;
  } else {
    status = 'optimal';
    headline = 'You’re on track tonight';
    const spare = focusCapacityMinutes - plannedMinutes;
    subtext = `${formatMinutes(plannedMinutes)} planned, ~${formatMinutes(spare)} of focus to spare before ${bedtimeLabel}.`;
  }

  return {
    status,
    headline,
    subtext,
    bedtimeLabel,
    bedtimeMs,
    minutesUntilBed,
    freeMinutes,
    focusCapacityMinutes,
    plannedMinutes,
    items,
    ranked,
    planned,
    deferred,
    atRisk,
    shortfallMinutes,
    plannedByTask,
    next,
    isCollegeDay
  };
}
